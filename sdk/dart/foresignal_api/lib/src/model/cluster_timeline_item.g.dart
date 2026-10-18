// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cluster_timeline_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClusterTimelineItemCWProxy {
  ClusterTimelineItem evidenceKey(String evidenceKey);

  ClusterTimelineItem at(DateTime at);

  ClusterTimelineItem type(ClusterTimelineItemTypeEnum type);

  ClusterTimelineItem title(String title);

  ClusterTimelineItem ref(String? ref);

  ClusterTimelineItem envelope(ObjectEnvelope? envelope);

  ClusterTimelineItem heldBy(List<String> heldBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClusterTimelineItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClusterTimelineItem(...).copyWith(id: 12, name: "My name")
  /// ```
  ClusterTimelineItem call({
    String evidenceKey,
    DateTime at,
    ClusterTimelineItemTypeEnum type,
    String title,
    String? ref,
    ObjectEnvelope? envelope,
    List<String> heldBy,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClusterTimelineItem.copyWith(...)` or call `instanceOfClusterTimelineItem.copyWith.fieldName(value)` for a single field.
class _$ClusterTimelineItemCWProxyImpl implements _$ClusterTimelineItemCWProxy {
  const _$ClusterTimelineItemCWProxyImpl(this._value);

  final ClusterTimelineItem _value;

  @override
  ClusterTimelineItem evidenceKey(String evidenceKey) =>
      call(evidenceKey: evidenceKey);

  @override
  ClusterTimelineItem at(DateTime at) => call(at: at);

  @override
  ClusterTimelineItem type(ClusterTimelineItemTypeEnum type) =>
      call(type: type);

  @override
  ClusterTimelineItem title(String title) => call(title: title);

  @override
  ClusterTimelineItem ref(String? ref) => call(ref: ref);

  @override
  ClusterTimelineItem envelope(ObjectEnvelope? envelope) =>
      call(envelope: envelope);

  @override
  ClusterTimelineItem heldBy(List<String> heldBy) => call(heldBy: heldBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClusterTimelineItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClusterTimelineItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClusterTimelineItem call({
    Object? evidenceKey = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? heldBy = const $CopyWithPlaceholder(),
  }) {
    return ClusterTimelineItem(
      evidenceKey:
          evidenceKey == const $CopyWithPlaceholder() || evidenceKey == null
          ? _value.evidenceKey
          // ignore: cast_nullable_to_non_nullable
          : evidenceKey as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ClusterTimelineItemTypeEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      envelope: envelope == const $CopyWithPlaceholder()
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope?,
      heldBy: heldBy == const $CopyWithPlaceholder() || heldBy == null
          ? _value.heldBy
          // ignore: cast_nullable_to_non_nullable
          : heldBy as List<String>,
    );
  }
}

extension $ClusterTimelineItemCopyWith on ClusterTimelineItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClusterTimelineItem.copyWith(...)` or `instanceOfClusterTimelineItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClusterTimelineItemCWProxy get copyWith =>
      _$ClusterTimelineItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClusterTimelineItem _$ClusterTimelineItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClusterTimelineItem', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['evidenceKey', 'at', 'type', 'title', 'heldBy'],
      );
      final val = ClusterTimelineItem(
        evidenceKey: $checkedConvert('evidenceKey', (v) => v as String),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ClusterTimelineItemTypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String),
        ref: $checkedConvert('ref', (v) => v as String?),
        envelope: $checkedConvert(
          'envelope',
          (v) => v == null
              ? null
              : ObjectEnvelope.fromJson(v as Map<String, dynamic>),
        ),
        heldBy: $checkedConvert(
          'heldBy',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ClusterTimelineItemToJson(
  ClusterTimelineItem instance,
) => <String, dynamic>{
  'evidenceKey': instance.evidenceKey,
  'at': instance.at.toIso8601String(),
  'type': _$ClusterTimelineItemTypeEnumEnumMap[instance.type]!,
  'title': instance.title,
  'ref': ?instance.ref,
  'envelope': ?instance.envelope?.toJson(),
  'heldBy': instance.heldBy,
};

const _$ClusterTimelineItemTypeEnumEnumMap = {
  ClusterTimelineItemTypeEnum.alert: 'alert',
  ClusterTimelineItemTypeEnum.finding: 'finding',
  ClusterTimelineItemTypeEnum.situationChange: 'situation-change',
  ClusterTimelineItemTypeEnum.hypothesisBandChange: 'hypothesis-band-change',
  ClusterTimelineItemTypeEnum.forecastResolution: 'forecast-resolution',
  ClusterTimelineItemTypeEnum.firstAppearance: 'first-appearance',
  ClusterTimelineItemTypeEnum.layerEdit: 'layer-edit',
  ClusterTimelineItemTypeEnum.captureGap: 'capture-gap',
  ClusterTimelineItemTypeEnum.capWarning: 'cap-warning',
  ClusterTimelineItemTypeEnum.capFallback: 'cap-fallback',
  ClusterTimelineItemTypeEnum.statusChange: 'status-change',
  ClusterTimelineItemTypeEnum.backfill: 'backfill',
  ClusterTimelineItemTypeEnum.record: 'record',
};
