/*
 * Copyright (c) 2014.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.util

import scala.annotation.unchecked.uncheckedVariance
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.BitSet
import scala.collection.mutable.{BitSet as MBitSet, Builder as MBuilder}
import scala.collection.{BuildFrom, immutable, mutable}

trait Enum
{
    thisenum =>

    type EnumVal <: Value

    private var vals = Vector[EnumVal]()
    def values = vals

    private final def addEnumVal(newVal:EnumVal):Int =
    {
        vals :+= newVal
        vals.indexOf(newVal)
    }

    def isDefinedAt(idx:Int) = vals.isDefinedAt(idx)

    def apply(ordinal:Int):EnumVal =
        if (vals.isDefinedAt(ordinal)) vals(ordinal)
        else null.asInstanceOf[EnumVal]

    protected trait Value extends ValueSubtype
    protected trait ValueSubtype extends Ordered[EnumVal]
    {
        def getThis = this.asInstanceOf[EnumVal]

        final val ordinal = addEnumVal(getThis)

        def name:String
        override def toString = name

        private[Enum] val outerEnum = thisenum

        override def compare(that:EnumVal) = this.ordinal-that.ordinal

        override def equals(other:Any) = other match
        {
            case that:Value => (outerEnum == that.outerEnum) && (ordinal == that.ordinal)
            case _ => false
        }

        override def hashCode = 31*(this.getClass.## +name.## +ordinal)

        def +(v:EnumVal) = ValSet(getThis, v)
        def ++(xs:TraversableOnce[EnumVal]) = (ValSet.newBuilder ++= xs).result()

        def until(v:EnumVal) = build(ordinal until v.ordinal)
        def to(v:EnumVal) = build(ordinal to v.ordinal)
        private def build(r:Range) =
        {
            val b = ValSet.newBuilder
            for (i <- r) b += apply(i)
            b.result()
        }
    }

    object ValOrdering extends Ordering[EnumVal]
    {
        override def compare(x:EnumVal, y:EnumVal) = x compare y
    }

    class ValSet(var set:BitSet) extends Set[EnumVal]
    with immutable.SortedSet[EnumVal]
    with Serializable
    {
        implicit def ordering = ValOrdering
        override def empty = ValSet.empty

        override def rangeImpl(from:Option[EnumVal], until:Option[EnumVal]) =
            new ValSet(set.rangeImpl(from.map(_.ordinal), until.map(_.ordinal)))

        override def contains(elem:EnumVal) = set contains elem.ordinal
        //override def +(elem:EnumVal) = new ValSet(set + elem.ordinal)
        //override def -(elem:EnumVal) = new ValSet(set - elem.ordinal)
        override def iterator = set.iterator map (id => thisenum(id))
        override def keysIteratorFrom(start: EnumVal) =
            throw new NotImplementedError("Please report this crash")

        // Members declared in scala.collection.immutable.SetOps
        def excl (elem: Enum.this.EnumVal): scala.collection.immutable.SortedSet[Enum.this.EnumVal] = new ValSet(set - elem.ordinal)
        def incl (elem: Enum.this.EnumVal): scala.collection.immutable.SortedSet[Enum.this.EnumVal] = new ValSet(set + elem.ordinal)
        def iteratorFrom (start: Enum.this.EnumVal @uncheckedVariance): Iterator[Enum.this.EnumVal @uncheckedVariance] = ???
    }

    object ValSet
    {
        val empty = new ValSet(BitSet.empty)

        def apply(elems:EnumVal*) = (newBuilder ++= elems).result()

        def newBuilder = new MBuilder[EnumVal, ValSet]
        {
            private val b = new MBitSet
            def addOne(x:EnumVal) = {b += x.ordinal; this}
            def clear() = b.clear()
            def result() = new ValSet(b.toImmutable)
        }

        implicit def canBuildFrom(): BuildFrom[_, _, _] = new CanBuildFrom[ValSet, EnumVal, ValSet] {
            def apply() = newBuilder
            def fromSpecific (from: Enum.this.ValSet) (it: IterableOnce[Enum.this.EnumVal]): Enum.this.ValSet = ???
            def newBuilder (from: Enum.this.ValSet): scala.collection.mutable.Builder[Enum.this.EnumVal, Enum.this.ValSet] = ???
        }
    }
}