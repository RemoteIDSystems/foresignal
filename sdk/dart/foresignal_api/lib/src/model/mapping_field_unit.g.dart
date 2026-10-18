// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_field_unit.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingFieldUnitCWProxy {
  MappingFieldUnit from(String from);

  MappingFieldUnit to(String to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingFieldUnit(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingFieldUnit(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingFieldUnit call({String from, String to});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingFieldUnit.copyWith(...)` or call `instanceOfMappingFieldUnit.copyWith.fieldName(value)` for a single field.
class _$MappingFieldUnitCWProxyImpl implements _$MappingFieldUnitCWProxy {
  const _$MappingFieldUnitCWProxyImpl(this._value);

  final MappingFieldUnit _value;

  @override
  MappingFieldUnit from(String from) => call(from: from);

  @override
  MappingFieldUnit to(String to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingFieldUnit(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingFieldUnit(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingFieldUnit call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return MappingFieldUnit(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String,
    );
  }
}

extension $MappingFieldUnitCopyWith on MappingFieldUnit {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingFieldUnit.copyWith(...)` or `instanceOfMappingFieldUnit.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingFieldUnitCWProxy get copyWith => _$MappingFieldUnitCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingFieldUnit _$MappingFieldUnitFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingFieldUnit', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['from', 'to']);
      final val = MappingFieldUnit(
        from: $checkedConvert('from', (v) => v as String),
        to: $checkedConvert('to', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$MappingFieldUnitToJson(MappingFieldUnit instance) =>
    <String, dynamic>{'from': instance.from, 'to': instance.to};
