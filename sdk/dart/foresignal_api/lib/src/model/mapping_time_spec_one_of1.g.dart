// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_time_spec_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingTimeSpecOneOf1CWProxy {
  MappingTimeSpecOneOf1 received(bool received);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTimeSpecOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTimeSpecOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingTimeSpecOneOf1 call({bool received});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingTimeSpecOneOf1.copyWith(...)` or call `instanceOfMappingTimeSpecOneOf1.copyWith.fieldName(value)` for a single field.
class _$MappingTimeSpecOneOf1CWProxyImpl
    implements _$MappingTimeSpecOneOf1CWProxy {
  const _$MappingTimeSpecOneOf1CWProxyImpl(this._value);

  final MappingTimeSpecOneOf1 _value;

  @override
  MappingTimeSpecOneOf1 received(bool received) => call(received: received);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTimeSpecOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTimeSpecOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingTimeSpecOneOf1 call({
    Object? received = const $CopyWithPlaceholder(),
  }) {
    return MappingTimeSpecOneOf1(
      received: received == const $CopyWithPlaceholder() || received == null
          ? _value.received
          // ignore: cast_nullable_to_non_nullable
          : received as bool,
    );
  }
}

extension $MappingTimeSpecOneOf1CopyWith on MappingTimeSpecOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingTimeSpecOneOf1.copyWith(...)` or `instanceOfMappingTimeSpecOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingTimeSpecOneOf1CWProxy get copyWith =>
      _$MappingTimeSpecOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingTimeSpecOneOf1 _$MappingTimeSpecOneOf1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingTimeSpecOneOf1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['received']);
  final val = MappingTimeSpecOneOf1(
    received: $checkedConvert('received', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$MappingTimeSpecOneOf1ToJson(
  MappingTimeSpecOneOf1 instance,
) => <String, dynamic>{'received': instance.received};
