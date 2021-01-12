package edu.utap.mypet.petEdit

data class PetProfile(
        // Auth information
        var name: String = "",
        var ownerUid: String = "",
        // Pet profile detailed
        var pictureUUIDs: String = "",
        var petName: String = "",
        var species: String="",
        var breed : String = "",
        var gender: String = "",
        var birthdate : String = "",
        var weight: String = "",
        var microchip: String = "",
        var rabiesTag: String = "",
        var others: String = "",

        //ID generated by firestore, used as primary key
        var profileID: String = ""
)