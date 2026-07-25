package services;
import dao.addresses;   //to perform database operations
import model.Address;
import java.math.BigDecimal; //used for latitude /longitude
import java.sql.SQLException;
import java.util.List;
public class address {
        private final addresses addressDAO;  //Creates a DAO object reference.
        // Default constructor
        public address() {
            this.addressDAO = new addresses();
        }
        // Constructor useful for testing
        public address(addresses addressDAO) {
            this.addressDAO = addressDAO;
        }
        // 1. Add a new address
        public boolean addAddress(Address address) throws SQLException { //address stored in database validation
            validateAddress(address);
            // If this is the user's first address, automatically make it default
            List<Address> existingAddresses =  //multiple address creation
                    addressDAO.getAddressesByUserId(address.getUserId());
            if (existingAddresses.isEmpty()) {
                address.setDefault(true);
            }
            return addressDAO.addAddress(address);
        }
        // 2. Get address by ID
        public Address getAddressById(int addressId) throws SQLException {
            validateAddressId(addressId);
            Address address = addressDAO.getAddressById(addressId);

            if (address == null) {
                throw new IllegalArgumentException(
                        "Address with ID " + addressId + " was not found."
                );
            }
            return address;
        }
        // 3. Get all addresses of a user
        public List<Address> getAddressesByUserId(int userId) throws SQLException {
            validateUserId(userId);
            return addressDAO.getAddressesByUserId(userId);
        }
        // 4. Get user's default address
        public Address getDefaultAddressByUserId(int userId) throws SQLException {
            validateUserId(userId);
            return addressDAO.getDefaultAddressByUserId(userId);
        }
        // 5. Update an existing address
        public boolean updateAddress(Address address) throws SQLException {
            if (address == null) {
                throw new IllegalArgumentException("Address cannot be null.");
            }
            validateAddressId(address.getAddressId());
            validateAddress(address);

            Address existingAddress =
                    addressDAO.getAddressById(address.getAddressId());

            if (existingAddress == null) {
                throw new IllegalArgumentException(
                        "Address with ID " + address.getAddressId() + " was not found."
                );
            }

            /*
             * Prevent one user from updating an address belonging
             * to another user.
             */
            if (existingAddress.getUserId() != address.getUserId()) {
                throw new IllegalArgumentException(
                        "This address does not belong to the specified user."
                );
            }

            return addressDAO.updateAddress(address);
        }

        // 6. Set an address as default
        public boolean setDefaultAddress(int userId, int addressId)
                throws SQLException {

            validateUserId(userId);
            validateAddressId(addressId);

            Address address = addressDAO.getAddressById(addressId);

            if (address == null) {
                throw new IllegalArgumentException(
                        "Address with ID " + addressId + " was not found."
                );
            }

            if (address.getUserId() != userId) {
                throw new IllegalArgumentException(
                        "This address does not belong to the specified user."
                );
            }

            // It is already the default address
            if (address.isDefault()) {
                return true;
            }

            return addressDAO.setDefaultAddress(userId, addressId);
        }

        // 7. Delete an address
        public boolean deleteAddress(int userId, int addressId)
                throws SQLException {

            validateUserId(userId);
            validateAddressId(addressId);

            Address address = addressDAO.getAddressById(addressId);

            if (address == null) {
                throw new IllegalArgumentException(
                        "Address with ID " + addressId + " was not found."
                );
            }

            if (address.getUserId() != userId) {
                throw new IllegalArgumentException(
                        "This address does not belong to the specified user."
                );
            }

            boolean wasDefault = address.isDefault();
            boolean deleted = addressDAO.deleteAddress(addressId);

            /*
             * If the deleted address was default, make another existing
             * address the default address.
             */
            if (deleted && wasDefault) {
                List<Address> remainingAddresses =
                        addressDAO.getAddressesByUserId(userId);

                if (!remainingAddresses.isEmpty()) {
                    Address newDefaultAddress = remainingAddresses.get(0);

                    addressDAO.setDefaultAddress(
                            userId,
                            newDefaultAddress.getAddressId()
                    );
                }
            }

            return deleted;
        }

        // Validate complete address data
        private void validateAddress(Address address) {
            if (address == null) {
                throw new IllegalArgumentException("Address cannot be null.");
            }

            validateUserId(address.getUserId());

            if (isBlank(address.getAddressType())) {
                throw new IllegalArgumentException(
                        "Address type is required."
                );
            }

            String addressType =
                    address.getAddressType().trim().toUpperCase();

            if (!isValidAddressType(addressType)) {
                throw new IllegalArgumentException(
                        "Address type must be BUSINESS, DELIVERY, FARM, or BILLING."
                );
            }

            // Store address type in a consistent uppercase format
            address.setAddressType(addressType);

            if (isBlank(address.getAddressLine())) {
                throw new IllegalArgumentException(
                        "Address line is required."
                );
            }

            if (isBlank(address.getCity())) {
                throw new IllegalArgumentException(
                        "City is required."
                );
            }

            if (isBlank(address.getArea())) {
                throw new IllegalArgumentException(
                        "Area is required."
                );
            }

            validateCoordinates(
                    address.getLatitude(),
                    address.getLongitude()
            );

            // Remove unnecessary spaces
            address.setAddressLine(address.getAddressLine().trim());
            address.setCity(address.getCity().trim());
            address.setArea(address.getArea().trim());

            if (address.getPostalCode() != null) {
                String postalCode = address.getPostalCode().trim();

                if (postalCode.isEmpty()) {
                    address.setPostalCode(null);
                } else {
                    address.setPostalCode(postalCode);
                }
            }
        }

        // Validate latitude and longitude
        private void validateCoordinates(
                BigDecimal latitude,
                BigDecimal longitude
        ) {
            /*
             * Either both coordinates should be provided,
             * or both should be null.
             */
            if ((latitude == null && longitude != null) ||
                    (latitude != null && longitude == null)) {

                throw new IllegalArgumentException(
                        "Latitude and longitude must be provided together."
                );
            }

            if (latitude != null) {
                BigDecimal minimumLatitude = new BigDecimal("-90");
                BigDecimal maximumLatitude = new BigDecimal("90");

                if (latitude.compareTo(minimumLatitude) < 0 ||
                        latitude.compareTo(maximumLatitude) > 0) {

                    throw new IllegalArgumentException(
                            "Latitude must be between -90 and 90."
                    );
                }
            }

            if (longitude != null) {
                BigDecimal minimumLongitude = new BigDecimal("-180");
                BigDecimal maximumLongitude = new BigDecimal("180");

                if (longitude.compareTo(minimumLongitude) < 0 ||
                        longitude.compareTo(maximumLongitude) > 0) {

                    throw new IllegalArgumentException(
                            "Longitude must be between -180 and 180."
                    );
                }
            }
        }

        private void validateUserId(int userId) {
            if (userId <= 0) {
                throw new IllegalArgumentException(
                        "User ID must be greater than zero."
                );
            }
        }

        private void validateAddressId(int addressId) {
            if (addressId <= 0) {
                throw new IllegalArgumentException(
                        "Address ID must be greater than zero."
                );
            }
        }

        private boolean isValidAddressType(String addressType) {
            return addressType.equals("BUSINESS") ||
                    addressType.equals("DELIVERY") ||
                    addressType.equals("FARM") ||
                    addressType.equals("BILLING");
        }

        private boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }




}
