// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'evidence_group.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EvidenceGroupCWProxy {
  EvidenceGroup id(String id);

  EvidenceGroup canonicalEventId(String canonicalEventId);

  EvidenceGroup originatorRoot(String originatorRoot);

  EvidenceGroup memberCount(int memberCount);

  EvidenceGroup firstSeq(int firstSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceGroup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceGroup(...).copyWith(id: 12, name: "My name")
  /// ```
  EvidenceGroup call({
    String id,
    String canonicalEventId,
    String originatorRoot,
    int memberCount,
    int firstSeq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEvidenceGroup.copyWith(...)` or call `instanceOfEvidenceGroup.copyWith.fieldName(value)` for a single field.
class _$EvidenceGroupCWProxyImpl implements _$EvidenceGroupCWProxy {
  const _$EvidenceGroupCWProxyImpl(this._value);

  final EvidenceGroup _value;

  @override
  EvidenceGroup id(String id) => call(id: id);

  @override
  EvidenceGroup canonicalEventId(String canonicalEventId) =>
      call(canonicalEventId: canonicalEventId);

  @override
  EvidenceGroup originatorRoot(String originatorRoot) =>
      call(originatorRoot: originatorRoot);

  @override
  EvidenceGroup memberCount(int memberCount) => call(memberCount: memberCount);

  @override
  EvidenceGroup firstSeq(int firstSeq) => call(firstSeq: firstSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceGroup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceGroup(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EvidenceGroup call({
    Object? id = const $CopyWithPlaceholder(),
    Object? canonicalEventId = const $CopyWithPlaceholder(),
    Object? originatorRoot = const $CopyWithPlaceholder(),
    Object? memberCount = const $CopyWithPlaceholder(),
    Object? firstSeq = const $CopyWithPlaceholder(),
  }) {
    return EvidenceGroup(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      canonicalEventId:
          canonicalEventId == const $CopyWithPlaceholder() ||
              canonicalEventId == null
          ? _value.canonicalEventId
          // ignore: cast_nullable_to_non_nullable
          : canonicalEventId as String,
      originatorRoot:
          originatorRoot == const $CopyWithPlaceholder() ||
              originatorRoot == null
          ? _value.originatorRoot
          // ignore: cast_nullable_to_non_nullable
          : originatorRoot as String,
      memberCount:
          memberCount == const $CopyWithPlaceholder() || memberCount == null
          ? _value.memberCount
          // ignore: cast_nullable_to_non_nullable
          : memberCount as int,
      firstSeq: firstSeq == const $CopyWithPlaceholder() || firstSeq == null
          ? _value.firstSeq
          // ignore: cast_nullable_to_non_nullable
          : firstSeq as int,
    );
  }
}

extension $EvidenceGroupCopyWith on EvidenceGroup {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEvidenceGroup.copyWith(...)` or `instanceOfEvidenceGroup.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EvidenceGroupCWProxy get copyWith => _$EvidenceGroupCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EvidenceGroup _$EvidenceGroupFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EvidenceGroup', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'canonicalEventId',
          'originatorRoot',
          'memberCount',
          'firstSeq',
        ],
      );
      final val = EvidenceGroup(
        id: $checkedConvert('id', (v) => v as String),
        canonicalEventId: $checkedConvert(
          'canonicalEventId',
          (v) => v as String,
        ),
        originatorRoot: $checkedConvert('originatorRoot', (v) => v as String),
        memberCount: $checkedConvert('memberCount', (v) => (v as num).toInt()),
        firstSeq: $checkedConvert('firstSeq', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$EvidenceGroupToJson(EvidenceGroup instance) =>
    <String, dynamic>{
      'id': instance.id,
      'canonicalEventId': instance.canonicalEventId,
      'originatorRoot': instance.originatorRoot,
      'memberCount': instance.memberCount,
      'firstSeq': instance.firstSeq,
    };
