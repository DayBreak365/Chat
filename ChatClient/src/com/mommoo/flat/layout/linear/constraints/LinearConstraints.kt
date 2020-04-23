package com.mommoo.flat.layout.linear.constraints

class LinearConstraints : Cloneable {
    var weight = 0
        private set
    var linearSpace = LinearSpace.WRAP_CONTENT
        private set

    constructor() {}
    constructor(weight: Int) {
        this.weight = weight
    }

    constructor(linearSpace: LinearSpace) {
        this.linearSpace = linearSpace
    }

    constructor(weight: Int, linearSpace: LinearSpace) {
        this.weight = weight
        this.linearSpace = linearSpace
    }

    fun setWeight(weight: Int): LinearConstraints {
        this.weight = weight
        return this
    }

    fun setLinearSpace(linearSpace: LinearSpace): LinearConstraints {
        this.linearSpace = linearSpace
        return this
    }

    public override fun clone(): LinearConstraints {
        return try {
            super.clone() as LinearConstraints
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
            this
        }
    }

    override fun toString(): String {
        return """
            [weight]      : $weight
            [LinearSpace] : $linearSpace
            """.trimIndent()
    }
}