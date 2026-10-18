// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_place_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolPlaceInputCWProxy {
  PolPlaceInput label(String? label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolPlaceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolPlaceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PolPlaceInput call({String? label});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolPlaceInput.copyWith(...)` or call `instanceOfPolPlaceInput.copyWith.fieldName(value)` for a single field.
class _$PolPlaceInputCWProxyImpl implements _$PolPlaceInputCWProxy {
  const _$PolPlaceInputCWProxyImpl(this._value);

  final PolPlaceInput _value;

  @override
  PolPlaceInput label(String? label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolPlaceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolPlaceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolPlaceInput call({Object? label = const $CopyWithPlaceholder()}) {
    return PolPlaceInput(
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
    );
  }
}

extension $PolPlaceInputCopyWith on PolPlaceInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolPlaceInput.copyWith(...)` or `instanceOfPolPlaceInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolPlaceInputCWProxy get copyWith => _$PolPlaceInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolPlaceInput _$PolPlaceInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PolPlaceInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['label']);
      final val = PolPlaceInput(
        label: $checkedConvert('label', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PolPlaceInputToJson(PolPlaceInput instance) =>
    <String, dynamic>{'label': instance.label};
