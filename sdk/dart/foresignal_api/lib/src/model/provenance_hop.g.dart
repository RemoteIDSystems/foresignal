// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'provenance_hop.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProvenanceHopCWProxy {
  ProvenanceHop instanceId(String instanceId);

  ProvenanceHop sourceId(String sourceId);

  ProvenanceHop externalId(String externalId);

  ProvenanceHop seq(int seq);

  ProvenanceHop ingestedAt(DateTime ingestedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProvenanceHop(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProvenanceHop(...).copyWith(id: 12, name: "My name")
  /// ```
  ProvenanceHop call({
    String instanceId,
    String sourceId,
    String externalId,
    int seq,
    DateTime ingestedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProvenanceHop.copyWith(...)` or call `instanceOfProvenanceHop.copyWith.fieldName(value)` for a single field.
class _$ProvenanceHopCWProxyImpl implements _$ProvenanceHopCWProxy {
  const _$ProvenanceHopCWProxyImpl(this._value);

  final ProvenanceHop _value;

  @override
  ProvenanceHop instanceId(String instanceId) => call(instanceId: instanceId);

  @override
  ProvenanceHop sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ProvenanceHop externalId(String externalId) => call(externalId: externalId);

  @override
  ProvenanceHop seq(int seq) => call(seq: seq);

  @override
  ProvenanceHop ingestedAt(DateTime ingestedAt) => call(ingestedAt: ingestedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProvenanceHop(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProvenanceHop(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProvenanceHop call({
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? ingestedAt = const $CopyWithPlaceholder(),
  }) {
    return ProvenanceHop(
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      ingestedAt:
          ingestedAt == const $CopyWithPlaceholder() || ingestedAt == null
          ? _value.ingestedAt
          // ignore: cast_nullable_to_non_nullable
          : ingestedAt as DateTime,
    );
  }
}

extension $ProvenanceHopCopyWith on ProvenanceHop {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProvenanceHop.copyWith(...)` or `instanceOfProvenanceHop.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProvenanceHopCWProxy get copyWith => _$ProvenanceHopCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProvenanceHop _$ProvenanceHopFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProvenanceHop', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'instanceId',
          'sourceId',
          'externalId',
          'seq',
          'ingestedAt',
        ],
      );
      final val = ProvenanceHop(
        instanceId: $checkedConvert('instanceId', (v) => v as String),
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        seq: $checkedConvert('seq', (v) => (v as num).toInt()),
        ingestedAt: $checkedConvert(
          'ingestedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ProvenanceHopToJson(ProvenanceHop instance) =>
    <String, dynamic>{
      'instanceId': instance.instanceId,
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'seq': instance.seq,
      'ingestedAt': instance.ingestedAt.toIso8601String(),
    };
