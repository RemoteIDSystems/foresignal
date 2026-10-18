// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_impact_request_record.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraImpactRequestRecordCWProxy {
  InfraImpactRequestRecord sourceId(String sourceId);

  InfraImpactRequestRecord externalId(String externalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactRequestRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactRequestRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraImpactRequestRecord call({String sourceId, String externalId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraImpactRequestRecord.copyWith(...)` or call `instanceOfInfraImpactRequestRecord.copyWith.fieldName(value)` for a single field.
class _$InfraImpactRequestRecordCWProxyImpl
    implements _$InfraImpactRequestRecordCWProxy {
  const _$InfraImpactRequestRecordCWProxyImpl(this._value);

  final InfraImpactRequestRecord _value;

  @override
  InfraImpactRequestRecord sourceId(String sourceId) =>
      call(sourceId: sourceId);

  @override
  InfraImpactRequestRecord externalId(String externalId) =>
      call(externalId: externalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactRequestRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactRequestRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraImpactRequestRecord call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
  }) {
    return InfraImpactRequestRecord(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
    );
  }
}

extension $InfraImpactRequestRecordCopyWith on InfraImpactRequestRecord {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraImpactRequestRecord.copyWith(...)` or `instanceOfInfraImpactRequestRecord.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraImpactRequestRecordCWProxy get copyWith =>
      _$InfraImpactRequestRecordCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraImpactRequestRecord _$InfraImpactRequestRecordFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('InfraImpactRequestRecord', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['sourceId', 'externalId']);
  final val = InfraImpactRequestRecord(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    externalId: $checkedConvert('externalId', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$InfraImpactRequestRecordToJson(
  InfraImpactRequestRecord instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'externalId': instance.externalId,
};
