// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_test_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingTestRequestCWProxy {
  MappingTestRequest mapping(RecordMapping? mapping);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingTestRequest call({RecordMapping? mapping});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingTestRequest.copyWith(...)` or call `instanceOfMappingTestRequest.copyWith.fieldName(value)` for a single field.
class _$MappingTestRequestCWProxyImpl implements _$MappingTestRequestCWProxy {
  const _$MappingTestRequestCWProxyImpl(this._value);

  final MappingTestRequest _value;

  @override
  MappingTestRequest mapping(RecordMapping? mapping) => call(mapping: mapping);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingTestRequest call({Object? mapping = const $CopyWithPlaceholder()}) {
    return MappingTestRequest(
      mapping: mapping == const $CopyWithPlaceholder()
          ? _value.mapping
          // ignore: cast_nullable_to_non_nullable
          : mapping as RecordMapping?,
    );
  }
}

extension $MappingTestRequestCopyWith on MappingTestRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingTestRequest.copyWith(...)` or `instanceOfMappingTestRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingTestRequestCWProxy get copyWith =>
      _$MappingTestRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingTestRequest _$MappingTestRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingTestRequest', json, ($checkedConvert) {
      final val = MappingTestRequest(
        mapping: $checkedConvert(
          'mapping',
          (v) => v == null
              ? null
              : RecordMapping.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MappingTestRequestToJson(MappingTestRequest instance) =>
    <String, dynamic>{'mapping': ?instance.mapping?.toJson()};
