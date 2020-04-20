package com.hedvig.notificationService.serviceIntegration.productPricing.client

enum class SwedishApartmentLineOfBusiness(val isStudent: Boolean) {
    RENT(isStudent = false),
    BRF(isStudent = false),
    STUDENT_RENT(isStudent = true),
    STUDENT_BRF(isStudent = true);
}
