package de.trbnb.mvvmbase.test.bindableproperty

import androidx.databinding.Bindable
import androidx.lifecycle.SavedStateHandle
import de.trbnb.mvvmbase.BaseViewModel
import de.trbnb.mvvmbase.MvvmBase
import de.trbnb.mvvmbase.bindableproperty.StateSaveOption
import de.trbnb.mvvmbase.bindableproperty.afterSet
import de.trbnb.mvvmbase.bindableproperty.beforeSet
import de.trbnb.mvvmbase.bindableproperty.bindableUByte
import de.trbnb.mvvmbase.bindableproperty.distinct
import de.trbnb.mvvmbase.bindableproperty.validate
import de.trbnb.mvvmbase.savedstate.BaseStateSavingViewModel
import de.trbnb.mvvmbase.test.BR
import de.trbnb.mvvmbase.test.TestPropertyChangedCallback
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class BindableUBytePropertyTests {
    @Test
    fun `is zero the default value without explicit assignment`() {
        val viewModel = object : BaseViewModel() {
            val property by bindableUByte()
        }

        assert(viewModel.property == 0.toUByte())
    }

    @Test
    fun `does value assignment work`() {
        val viewModel = object : BaseViewModel() {
            var property by bindableUByte()
        }

        val newValue = 3.toUByte()
        assert(viewModel.property != newValue)

        viewModel.property = newValue
        assert(viewModel.property == newValue)
    }

    @Test
    fun `is afterSet() called`() {
        val newValue = 7.toUByte()
        val viewModel = object : BaseViewModel() {
            var property by bindableUByte()
                .afterSet { new -> assert(newValue == new) }
        }

        viewModel.property = newValue
    }

    @Test
    fun `is distinct() prohibting afterSet() invocation`() {
        val value = 4.toUByte()
        var afterSetWasCalled = false
        val viewModel = object : BaseViewModel() {
            var propery by bindableUByte(value)
                .distinct()
                .afterSet { afterSetWasCalled = true }
        }

        viewModel.propery = value
        assert(!afterSetWasCalled)
    }

    @Test
    fun `is beforeSet() called`() {
        val oldValue = 4.toUByte()
        val newValue = 9.toUByte()
        assert(oldValue != newValue)

        val viewModel = object : BaseViewModel() {
            var property by bindableUByte(oldValue)
                .beforeSet { old, new ->
                    assert(old == oldValue)
                    assert(new == newValue)
                }
        }

        assert(viewModel.property == oldValue)
        viewModel.property = newValue
    }

    @Test
    fun `is validate() called`() {
        val oldValue = 0.toUByte()
        val newValue = 50.toUByte()
        val maxValue = 25.toUByte()
        assert(oldValue != newValue)

        val viewModel = object : BaseViewModel() {
            var property by bindableUByte(defaultValue = oldValue)
                .validate { old, new ->
                    assert(old == oldValue)
                    assert(new == newValue)
                    return@validate new.coerceAtMost(maxValue)
                }
        }

        assert(viewModel.property == oldValue)
        viewModel.property = newValue
        assert(viewModel.property == maxValue)
    }

    @Test
    fun `is correct field ID used for notifyPropertyChanged()`() {
        MvvmBase.init<BR>()
        val propertyChangedCallback = TestPropertyChangedCallback()
        val manualFieldId = BR.vm
        val viewModel = ViewModelWithBindable(manualFieldId)
        viewModel.addOnPropertyChangedCallback(propertyChangedCallback)

        viewModel.property = 5.toUByte()
        assert(BR.property in propertyChangedCallback.changedPropertyIds)
        propertyChangedCallback.clear()

        viewModel.manualProperty = 7.toUByte()
        assert(manualFieldId in propertyChangedCallback.changedPropertyIds)
        propertyChangedCallback.clear()
    }

    @Test
    fun `field ID falls back to _all`() {
        // undo MvvmBase.init<BR>() call
        MvvmBase.init<Unit>()

        val propertyChangedCallback = TestPropertyChangedCallback()
        val manualFieldId = BR.vm
        val viewModel = ViewModelWithBindable(manualFieldId)
        viewModel.addOnPropertyChangedCallback(propertyChangedCallback)

        viewModel.property = 5.toUByte()
        assert(BR._all in propertyChangedCallback.changedPropertyIds)
        propertyChangedCallback.clear()

        viewModel.manualProperty = 2.toUByte()
        assert(manualFieldId in propertyChangedCallback.changedPropertyIds)
        propertyChangedCallback.clear()
    }

    class ViewModelWithBindable(fieldId: Int) : BaseViewModel() {
        @get:Bindable
        var property by bindableUByte()

        @get:Bindable
        var manualProperty by bindableUByte(fieldId = fieldId)
    }

    @Test
    fun `automatic StateSaveOption is set correctly`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AutomaticSavedStateViewModel(savedStateHandle)

        val newValue = UByte.MAX_VALUE
        viewModel.supportedAutomatic = newValue
        assert(savedStateHandle.get<Byte>("supportedAutomatic")?.toUByte() == newValue)
    }

    class AutomaticSavedStateViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) : BaseStateSavingViewModel(savedStateHandle) {
        @get:Bindable
        var supportedAutomatic by bindableUByte()
    }

    @Test
    fun `manual StateSaveOption is working correctly`() {
        val savedStateHandle = SavedStateHandle()
        val key = "Bar"
        val viewModel = ManualSavedStateViewModel(key, savedStateHandle)

        val newValue = 9.toUByte()
        viewModel.property = newValue
        assert(savedStateHandle.get<Byte>(key)?.toUByte() == newValue)
    }

    class ManualSavedStateViewModel(
        key: String,
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) : BaseStateSavingViewModel(savedStateHandle) {
        @get:Bindable
        var property by bindableUByte(stateSaveOption = StateSaveOption.Manual(key))
    }

    @Test
    fun `none StateSaveOption is working correctly`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = NoneSavedStateViewModel(savedStateHandle)

        val newValue = 6.toUByte()
        viewModel.property = newValue
        assert(savedStateHandle.keys().isEmpty())
    }

    class NoneSavedStateViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) : BaseStateSavingViewModel(savedStateHandle) {
        @get:Bindable
        var property by bindableUByte(stateSaveOption = StateSaveOption.None)
    }

    @Test
    fun `distinct does prevent notifyPropertyChanged`() {
        val propertyChangedCallback = TestPropertyChangedCallback()
        val viewModel = ViewModelWithDistinct().apply {
            addOnPropertyChangedCallback(propertyChangedCallback)
        }

        viewModel.property = 3.toUByte()
        assert(BR.property in propertyChangedCallback.changedPropertyIds)
        propertyChangedCallback.clear()

        viewModel.property = 3.toUByte()
        assert(BR.property !in propertyChangedCallback.changedPropertyIds)
    }

    class ViewModelWithDistinct : BaseViewModel() {
        @get:Bindable
        var property by bindableUByte()
            .distinct()
    }
}
