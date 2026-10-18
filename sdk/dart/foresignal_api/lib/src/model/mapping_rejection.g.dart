// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_rejection.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingRejectionCWProxy {
  MappingRejection index(int index);

  MappingRejection reason(String reason);

  MappingRejection externalId(String? externalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingRejection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingRejection(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingRejection call({int index, String reason, String? externalId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingRejection.copyWith(...)` or call `instanceOfMappingRejection.copyWith.fieldName(value)` for a single field.
class _$MappingRejectionCWProxyImpl implements _$MappingRejectionCWProxy {
  const _$MappingRejectionCWProxyImpl(this._value);

  final MappingRejection _value;

  @override
  MappingRejection index(int index) => call(index: index);

  @override
  MappingRejection reason(String reason) => call(reason: reason);

  @override
  MappingRejection externalId(String? externalId) =>
      call(externalId: externalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingRejection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingRejection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingRejection call({
    Object? index = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
  }) {
    return MappingRejection(
      index: index == const $CopyWithPlaceholder() || index == null
          ? _value.index
          // ignore: cast_nullable_to_non_nullable
          : index as int,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
    );
  }
}

extension $MappingRejectionCopyWith on MappingRejection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingRejection.copyWith(...)` or `instanceOfMappingRejection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingRejectionCWProxy get copyWith => _$MappingRejectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingRejection _$MappingRejectionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingRejection', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['index', 'reason']);
      final val = MappingRejection(
        index: $checkedConvert('index', (v) => (v as num).toInt()),
        reason: $checkedConvert('reason', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MappingRejectionToJson(MappingRejection instance) =>
    <String, dynamic>{
      'index': instance.index,
      'reason': instance.reason,
      'externalId': ?instance.externalId,
    };
