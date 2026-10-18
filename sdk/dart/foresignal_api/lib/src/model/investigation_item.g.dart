// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'investigation_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InvestigationItemCWProxy {
  InvestigationItem id(String id);

  InvestigationItem itemType(InvestigationItemItemTypeEnum itemType);

  InvestigationItem ref(String ref);

  InvestigationItem seq(int? seq);

  InvestigationItem snapshot(Map<String, Object>? snapshot);

  InvestigationItem note(String? note);

  InvestigationItem addedAt(DateTime addedAt);

  InvestigationItem addedBy(String addedBy);

  InvestigationItem objectRef(String? objectRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationItem(...).copyWith(id: 12, name: "My name")
  /// ```
  InvestigationItem call({
    String id,
    InvestigationItemItemTypeEnum itemType,
    String ref,
    int? seq,
    Map<String, Object>? snapshot,
    String? note,
    DateTime addedAt,
    String addedBy,
    String? objectRef,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInvestigationItem.copyWith(...)` or call `instanceOfInvestigationItem.copyWith.fieldName(value)` for a single field.
class _$InvestigationItemCWProxyImpl implements _$InvestigationItemCWProxy {
  const _$InvestigationItemCWProxyImpl(this._value);

  final InvestigationItem _value;

  @override
  InvestigationItem id(String id) => call(id: id);

  @override
  InvestigationItem itemType(InvestigationItemItemTypeEnum itemType) =>
      call(itemType: itemType);

  @override
  InvestigationItem ref(String ref) => call(ref: ref);

  @override
  InvestigationItem seq(int? seq) => call(seq: seq);

  @override
  InvestigationItem snapshot(Map<String, Object>? snapshot) =>
      call(snapshot: snapshot);

  @override
  InvestigationItem note(String? note) => call(note: note);

  @override
  InvestigationItem addedAt(DateTime addedAt) => call(addedAt: addedAt);

  @override
  InvestigationItem addedBy(String addedBy) => call(addedBy: addedBy);

  @override
  InvestigationItem objectRef(String? objectRef) => call(objectRef: objectRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InvestigationItem call({
    Object? id = const $CopyWithPlaceholder(),
    Object? itemType = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? snapshot = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? addedAt = const $CopyWithPlaceholder(),
    Object? addedBy = const $CopyWithPlaceholder(),
    Object? objectRef = const $CopyWithPlaceholder(),
  }) {
    return InvestigationItem(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      itemType: itemType == const $CopyWithPlaceholder() || itemType == null
          ? _value.itemType
          // ignore: cast_nullable_to_non_nullable
          : itemType as InvestigationItemItemTypeEnum,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      snapshot: snapshot == const $CopyWithPlaceholder()
          ? _value.snapshot
          // ignore: cast_nullable_to_non_nullable
          : snapshot as Map<String, Object>?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      addedAt: addedAt == const $CopyWithPlaceholder() || addedAt == null
          ? _value.addedAt
          // ignore: cast_nullable_to_non_nullable
          : addedAt as DateTime,
      addedBy: addedBy == const $CopyWithPlaceholder() || addedBy == null
          ? _value.addedBy
          // ignore: cast_nullable_to_non_nullable
          : addedBy as String,
      objectRef: objectRef == const $CopyWithPlaceholder()
          ? _value.objectRef
          // ignore: cast_nullable_to_non_nullable
          : objectRef as String?,
    );
  }
}

extension $InvestigationItemCopyWith on InvestigationItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInvestigationItem.copyWith(...)` or `instanceOfInvestigationItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InvestigationItemCWProxy get copyWith =>
      _$InvestigationItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InvestigationItem _$InvestigationItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InvestigationItem', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'itemType', 'ref', 'addedAt', 'addedBy'],
      );
      final val = InvestigationItem(
        id: $checkedConvert('id', (v) => v as String),
        itemType: $checkedConvert(
          'itemType',
          (v) => $enumDecode(_$InvestigationItemItemTypeEnumEnumMap, v),
        ),
        ref: $checkedConvert('ref', (v) => v as String),
        seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
        snapshot: $checkedConvert(
          'snapshot',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        note: $checkedConvert('note', (v) => v as String?),
        addedAt: $checkedConvert('addedAt', (v) => DateTime.parse(v as String)),
        addedBy: $checkedConvert('addedBy', (v) => v as String),
        objectRef: $checkedConvert('objectRef', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$InvestigationItemToJson(InvestigationItem instance) =>
    <String, dynamic>{
      'id': instance.id,
      'itemType': _$InvestigationItemItemTypeEnumEnumMap[instance.itemType]!,
      'ref': instance.ref,
      'seq': ?instance.seq,
      'snapshot': ?instance.snapshot,
      'note': ?instance.note,
      'addedAt': instance.addedAt.toIso8601String(),
      'addedBy': instance.addedBy,
      'objectRef': ?instance.objectRef,
    };

const _$InvestigationItemItemTypeEnumEnumMap = {
  InvestigationItemItemTypeEnum.event: 'event',
  InvestigationItemItemTypeEnum.alert: 'alert',
  InvestigationItemItemTypeEnum.record: 'record',
  InvestigationItemItemTypeEnum.anomaly: 'anomaly',
  InvestigationItemItemTypeEnum.place: 'place',
  InvestigationItemItemTypeEnum.briefingRun: 'briefing_run',
  InvestigationItemItemTypeEnum.note: 'note',
  InvestigationItemItemTypeEnum.evidenceItem: 'evidence-item',
  InvestigationItemItemTypeEnum.attachment: 'attachment',
  InvestigationItemItemTypeEnum.object: 'object',
};
