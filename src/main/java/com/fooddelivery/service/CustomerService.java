package com.fooddelivery.service;

import com.fooddelivery.dto.AddressRequest;
import com.fooddelivery.dto.AddressResponse;
import com.fooddelivery.dto.CustomerResponse;
import com.fooddelivery.dto.CustomerUpdateRequest;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.Customer;
import com.fooddelivery.model.CustomerAddress;
import com.fooddelivery.repository.CustomerAddressRepository;
import com.fooddelivery.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerAddressRepository addressRepository;

    public CustomerResponse getCustomerProfile(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return convertToResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomerProfile(Long userId, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.setName(request.getName());
        customer = customerRepository.save(customer);
        return convertToResponse(customer);
    }

    @Transactional
    @CacheEvict(value = "customerAddresses", key = "#userId")
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // If this is set as default, unset other default addresses
        if (request.getIsDefault() != null && request.getIsDefault()) {
            addressRepository.findByCustomerIdAndIsDefaultTrue(customer.getId())
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        addressRepository.save(existingDefault);
                    });
        }

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setPincode(request.getPincode());
        address.setLat(request.getLat());
        address.setLongitude(request.getLongitude());
        address.setAddressType(request.getAddressType());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        address = addressRepository.save(address);
        return convertToAddressResponse(address);
    }

    @Cacheable(value = "customerAddresses", key = "#userId")
    public List<AddressResponse> getCustomerAddresses(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<CustomerAddress> addresses = addressRepository.findByCustomerId(customer.getId());
        return addresses.stream()
                .map(this::convertToAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "customerAddresses", key = "#userId")
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        CustomerAddress address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Unset other default addresses
        addressRepository.findByCustomerIdAndIsDefaultTrue(customer.getId())
                .ifPresent(existingDefault -> {
                    if (!existingDefault.getId().equals(addressId)) {
                        existingDefault.setIsDefault(false);
                        addressRepository.save(existingDefault);
                    }
                });

        address.setIsDefault(true);
        address = addressRepository.save(address);
        return convertToAddressResponse(address);
    }

    @Transactional
    @CacheEvict(value = "customerAddresses", key = "#userId")
    public void deleteAddress(Long userId, Long addressId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        CustomerAddress address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
    }

    private CustomerResponse convertToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setProfileImage(customer.getProfileImage());
        response.setWalletBalance(customer.getWalletBalance());
        if (customer.getUser() != null) {
            response.setEmail(customer.getUser().getEmail());
            response.setPhone(customer.getUser().getPhone());
        }
        return response;
    }

    private AddressResponse convertToAddressResponse(CustomerAddress address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setPincode(address.getPincode());
        response.setLat(address.getLat());
        response.setLongitude(address.getLongitude());
        response.setAddressType(address.getAddressType());
        response.setIsDefault(address.getIsDefault());
        return response;
    }
}

