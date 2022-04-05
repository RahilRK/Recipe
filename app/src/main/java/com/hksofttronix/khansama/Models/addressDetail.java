package com.hksofttronix.khansama.Models;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

public class addressDetail {

    String localid,userId,name, mobileNumber,addressId,address,nearBy,cityId,city,stateId,state,pincode;

    public addressDetail() {
    }

    @Exclude
    public String getLocalid() {
        return localid;
    }

    public void setLocalid(String localid) {
        this.localid = localid;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNearBy() {
        return nearBy;
    }

    public void setNearBy(String nearBy) {
        this.nearBy = nearBy;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        addressDetail that = (addressDetail) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(mobileNumber, that.mobileNumber) &&
                Objects.equals(addressId, that.addressId) &&
                Objects.equals(address, that.address) &&
                Objects.equals(nearBy, that.nearBy) &&
                Objects.equals(cityId, that.cityId) &&
                Objects.equals(city, that.city) &&
                Objects.equals(stateId, that.stateId) &&
                Objects.equals(state, that.state) &&
                Objects.equals(pincode, that.pincode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, name, mobileNumber, addressId, address, nearBy, cityId, city, stateId, state, pincode);
    }
}
