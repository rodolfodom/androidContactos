package fes.aragon.proyectofinal




class Contact(id:String, name: String, lastName: String, phone: String, email: String, avatar: String) {
    private lateinit var name: String
    private lateinit var lastName: String
    private lateinit var phone: String
    private lateinit var email: String
    private lateinit var avatar: String
    private lateinit var id: String

    init {
        this.name = name
        this.lastName = lastName
        this.phone = phone
        this.email = email
        this.avatar = avatar
        this.id = id
    }

    fun getName(): String {
        return this.name
    }

    fun getLastName(): String {
        return this.lastName
    }

    fun getPhone(): String {
        return this.phone
    }

    fun getEmail(): String {
        return this.email
    }

    fun getAvatar(): String {
        return this.avatar
    }

    fun setName(name: String) {
        this.name = name
    }

    fun setLastName(lastName: String) {
        this.lastName = lastName
    }

    fun setPhone(phone: String) {
        this.phone = phone
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun setAvatar(avatar: String) {
        this.avatar = avatar
    }

    fun getId(): String {
        return this.id
    }

    fun setId(id: String) {
        this.id = id
    }

}