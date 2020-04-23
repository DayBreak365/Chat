package com.mommoo.flat.layout.linear

import com.mommoo.flat.layout.exception.MismatchException
import com.mommoo.flat.layout.linear.constraints.LinearConstraints
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager2
import java.io.Serializable

class LinearLayout @JvmOverloads constructor(private var orientation: Orientation = Orientation.HORIZONTAL, private var gap: Int = 10, private var alignment: Alignment = Alignment.START) : LinearLayoutProperty, LayoutManager2, Serializable {
    private val VALIDATOR = Validator()
    private val FINDER = ConstraintsFinder()
    private val CALCULATOR = LinearAreaCalculator()
    private var weightSum = 0

    constructor(gap: Int) : this(Orientation.HORIZONTAL, gap, Alignment.START) {}
    constructor(orientation: Orientation, alignment: Alignment) : this(orientation, 10, alignment) {}
    constructor(gap: Int, alignment: Alignment) : this(Orientation.HORIZONTAL, gap, alignment) {}
    constructor(alignment: Alignment) : this(Orientation.HORIZONTAL, 10, alignment) {}

    override fun preferredLayoutSize(parent: Container): Dimension {
        synchronized(parent.treeLock) {
            val dimension = Dimension(0, 0)
            for (component in parent.components) {
                if (!component.isVisible) continue
                val compDimen = component.preferredSize
                if (orientation == Orientation.HORIZONTAL) {
                    dimension.width += compDimen.width
                    dimension.height = Math.max(dimension.height, compDimen.height)
                } else {
                    dimension.width = Math.max(dimension.width, compDimen.width)
                    dimension.height += compDimen.height
                }
            }

            /* Add padding size */
            val insets = parent.insets
            dimension.width += insets.left + insets.right
            dimension.height += insets.top + insets.bottom
            val occupiedGapSize = (parent.componentCount - 1) * gap

            /* Add gap size */if (orientation == Orientation.HORIZONTAL) {
            dimension.width += occupiedGapSize
        } else {
            dimension.height += occupiedGapSize
        }
            return dimension
        }
    }

    override fun maximumLayoutSize(target: Container): Dimension {
        return Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
    }

    override fun minimumLayoutSize(parent: Container): Dimension {
        synchronized(parent.treeLock) {
            val dimension = Dimension(0, 0)
            val insets = parent.insets
            dimension.width = insets.left + insets.right
            dimension.height = insets.top + insets.bottom
            return dimension
        }
    }

    override fun getLayoutAlignmentX(target: Container): Float {
        return 0.5f
    }

    override fun getLayoutAlignmentY(target: Container): Float {
        return 0.5f
    }

    override fun invalidateLayout(target: Container) {}
    override fun addLayoutComponent(comp: Component, constraints: Any) {
        synchronized(comp.treeLock) {
            if (constraints is LinearConstraints) {
                val linearConstraints = constraints.clone()
                FINDER.put(comp, linearConstraints)
            }
        }
    }

    override fun addLayoutComponent(name: String, comp: Component) {
        throw UnsupportedOperationException("Don't support method :: addLayoutComponent(String name, Component comp)")
    }

    override fun removeLayoutComponent(comp: Component) {
        synchronized(comp.treeLock) { FINDER.remove(comp) }
    }

    override fun layoutContainer(container: Container) {
        synchronized(container.treeLock) {
            if (VALIDATOR.isValidate(container)) {
                val bounds = CALCULATOR.getBounds(this, container, FINDER)
                var index = 0
                for (comp in container.components) {
                    comp.bounds = bounds[index++]
                }
            }
        }
    }

    fun setOrientation(orientation: Orientation) {
        this.orientation = orientation
    }

    override fun getOrientation(): Orientation {
        return orientation
    }

    fun setGap(gap: Int) {
        this.gap = gap
    }

    override fun getGap(): Int {
        return gap
    }

    override fun getAlignment(): Alignment {
        return alignment
    }

    fun setAlignment(alignment: Alignment) {
        this.alignment = alignment
    }

    override fun getWeightSum(): Int {
        return weightSum
    }

    fun setWeightSum(weightSum: Int) {
        require(weightSum > 0) { "weightSum can not smaller than zero value" }
        this.weightSum = weightSum
        VALIDATOR.setAutoWeightSum(false)
    }

    private inner class Validator {
        private var isAutoWeightSum = true
        fun isValidate(container: Container): Boolean {
            validateWeightSumIfNotAuto()
            sumWeightsIfAutoMode()
            return container.componentCount > 0
        }

        private fun validateWeightSumIfNotAuto() {
            if (!isAutoWeightSum && FINDER.weightSum > weightSum) {
                try {
                    throw MismatchException()
                } catch (e: MismatchException) {
                    e.printStackTrace()
                }
            }
        }

        private fun sumWeightsIfAutoMode() {
            if (isAutoWeightSum) {
                weightSum = FINDER.weightSum
            }
        }

        fun setAutoWeightSum(autoWeightSum: Boolean) {
            isAutoWeightSum = autoWeightSum
        }
    }

}