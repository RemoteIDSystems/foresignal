// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'legend_tick.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LegendTickCWProxy {
  LegendTick value(num value);

  LegendTick label(String label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LegendTick(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LegendTick(...).copyWith(id: 12, name: "My name")
  /// ```
  LegendTick call({num value, String label});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLegendTick.copyWith(...)` or call `instanceOfLegendTick.copyWith.fieldName(value)` for a single field.
class _$LegendTickCWProxyImpl implements _$LegendTickCWProxy {
  const _$LegendTickCWProxyImpl(this._value);

  final LegendTick _value;

  @override
  LegendTick value(num value) => call(value: value);

  @override
  LegendTick label(String label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LegendTick(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LegendTick(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LegendTick call({
    Object? value = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return LegendTick(
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
    );
  }
}

extension $LegendTickCopyWith on LegendTick {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLegendTick.copyWith(...)` or `instanceOfLegendTick.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LegendTickCWProxy get copyWith => _$LegendTickCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LegendTick _$LegendTickFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LegendTick', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['value', 'label']);
      final val = LegendTick(
        value: $checkedConvert('value', (v) => v as num),
        label: $checkedConvert('label', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$LegendTickToJson(LegendTick instance) =>
    <String, dynamic>{'value': instance.value, 'label': instance.label};
