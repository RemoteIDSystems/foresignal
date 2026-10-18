// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_thread_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleThreadItemCWProxy {
  ChronicleThreadItem id(int id);

  ChronicleThreadItem chronicleId(String chronicleId);

  ChronicleThreadItem type(ChronicleThreadItemTypeEnum type);

  ChronicleThreadItem at(DateTime at);

  ChronicleThreadItem seq(int? seq);

  ChronicleThreadItem layerId(String? layerId);

  ChronicleThreadItem title(String title);

  ChronicleThreadItem summary(String? summary);

  ChronicleThreadItem ref(String? ref);

  ChronicleThreadItem envelope(ObjectEnvelope? envelope);

  ChronicleThreadItem snapshot(Map<String, Object>? snapshot);

  ChronicleThreadItem gap(ChronicleThreadItemGap? gap);

  ChronicleThreadItem backfilled(bool? backfilled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleThreadItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleThreadItem(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleThreadItem call({
    int id,
    String chronicleId,
    ChronicleThreadItemTypeEnum type,
    DateTime at,
    int? seq,
    String? layerId,
    String title,
    String? summary,
    String? ref,
    ObjectEnvelope? envelope,
    Map<String, Object>? snapshot,
    ChronicleThreadItemGap? gap,
    bool? backfilled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleThreadItem.copyWith(...)` or call `instanceOfChronicleThreadItem.copyWith.fieldName(value)` for a single field.
class _$ChronicleThreadItemCWProxyImpl implements _$ChronicleThreadItemCWProxy {
  const _$ChronicleThreadItemCWProxyImpl(this._value);

  final ChronicleThreadItem _value;

  @override
  ChronicleThreadItem id(int id) => call(id: id);

  @override
  ChronicleThreadItem chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleThreadItem type(ChronicleThreadItemTypeEnum type) =>
      call(type: type);

  @override
  ChronicleThreadItem at(DateTime at) => call(at: at);

  @override
  ChronicleThreadItem seq(int? seq) => call(seq: seq);

  @override
  ChronicleThreadItem layerId(String? layerId) => call(layerId: layerId);

  @override
  ChronicleThreadItem title(String title) => call(title: title);

  @override
  ChronicleThreadItem summary(String? summary) => call(summary: summary);

  @override
  ChronicleThreadItem ref(String? ref) => call(ref: ref);

  @override
  ChronicleThreadItem envelope(ObjectEnvelope? envelope) =>
      call(envelope: envelope);

  @override
  ChronicleThreadItem snapshot(Map<String, Object>? snapshot) =>
      call(snapshot: snapshot);

  @override
  ChronicleThreadItem gap(ChronicleThreadItemGap? gap) => call(gap: gap);

  @override
  ChronicleThreadItem backfilled(bool? backfilled) =>
      call(backfilled: backfilled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleThreadItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleThreadItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleThreadItem call({
    Object? id = const $CopyWithPlaceholder(),
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? layerId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? snapshot = const $CopyWithPlaceholder(),
    Object? gap = const $CopyWithPlaceholder(),
    Object? backfilled = const $CopyWithPlaceholder(),
  }) {
    return ChronicleThreadItem(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleThreadItemTypeEnum,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      layerId: layerId == const $CopyWithPlaceholder()
          ? _value.layerId
          // ignore: cast_nullable_to_non_nullable
          : layerId as String?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      envelope: envelope == const $CopyWithPlaceholder()
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope?,
      snapshot: snapshot == const $CopyWithPlaceholder()
          ? _value.snapshot
          // ignore: cast_nullable_to_non_nullable
          : snapshot as Map<String, Object>?,
      gap: gap == const $CopyWithPlaceholder()
          ? _value.gap
          // ignore: cast_nullable_to_non_nullable
          : gap as ChronicleThreadItemGap?,
      backfilled: backfilled == const $CopyWithPlaceholder()
          ? _value.backfilled
          // ignore: cast_nullable_to_non_nullable
          : backfilled as bool?,
    );
  }
}

extension $ChronicleThreadItemCopyWith on ChronicleThreadItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleThreadItem.copyWith(...)` or `instanceOfChronicleThreadItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleThreadItemCWProxy get copyWith =>
      _$ChronicleThreadItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleThreadItem _$ChronicleThreadItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleThreadItem', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'chronicleId', 'type', 'at', 'title'],
  );
  final val = ChronicleThreadItem(
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    chronicleId: $checkedConvert('chronicleId', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleThreadItemTypeEnumEnumMap, v),
    ),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
    layerId: $checkedConvert('layerId', (v) => v as String?),
    title: $checkedConvert('title', (v) => v as String),
    summary: $checkedConvert('summary', (v) => v as String?),
    ref: $checkedConvert('ref', (v) => v as String?),
    envelope: $checkedConvert(
      'envelope',
      (v) =>
          v == null ? null : ObjectEnvelope.fromJson(v as Map<String, dynamic>),
    ),
    snapshot: $checkedConvert(
      'snapshot',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    gap: $checkedConvert(
      'gap',
      (v) => v == null
          ? null
          : ChronicleThreadItemGap.fromJson(v as Map<String, dynamic>),
    ),
    backfilled: $checkedConvert('backfilled', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleThreadItemToJson(
  ChronicleThreadItem instance,
) => <String, dynamic>{
  'id': instance.id,
  'chronicleId': instance.chronicleId,
  'type': _$ChronicleThreadItemTypeEnumEnumMap[instance.type]!,
  'at': instance.at.toIso8601String(),
  'seq': ?instance.seq,
  'layerId': ?instance.layerId,
  'title': instance.title,
  'summary': ?instance.summary,
  'ref': ?instance.ref,
  'envelope': ?instance.envelope?.toJson(),
  'snapshot': ?instance.snapshot,
  'gap': ?instance.gap?.toJson(),
  'backfilled': ?instance.backfilled,
};

const _$ChronicleThreadItemTypeEnumEnumMap = {
  ChronicleThreadItemTypeEnum.alert: 'alert',
  ChronicleThreadItemTypeEnum.finding: 'finding',
  ChronicleThreadItemTypeEnum.situationChange: 'situation-change',
  ChronicleThreadItemTypeEnum.hypothesisBandChange: 'hypothesis-band-change',
  ChronicleThreadItemTypeEnum.forecastResolution: 'forecast-resolution',
  ChronicleThreadItemTypeEnum.firstAppearance: 'first-appearance',
  ChronicleThreadItemTypeEnum.layerEdit: 'layer-edit',
  ChronicleThreadItemTypeEnum.captureGap: 'capture-gap',
  ChronicleThreadItemTypeEnum.capWarning: 'cap-warning',
  ChronicleThreadItemTypeEnum.capFallback: 'cap-fallback',
  ChronicleThreadItemTypeEnum.statusChange: 'status-change',
  ChronicleThreadItemTypeEnum.backfill: 'backfill',
  ChronicleThreadItemTypeEnum.narrativeChange: 'narrative-change',
  ChronicleThreadItemTypeEnum.comparisonChange: 'comparison-change',
};
