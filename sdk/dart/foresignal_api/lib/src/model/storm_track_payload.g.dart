// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'storm_track_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StormTrackPayloadCWProxy {
  StormTrackPayload cell(StormTrackPayloadCell cell);

  StormTrackPayload history(List<StormTrackPayloadHistoryInner>? history);

  StormTrackPayload motion(StormTrackPayloadMotion motion);

  StormTrackPayload trend(StormTrackPayloadTrend? trend);

  StormTrackPayload lightningJump(
    StormTrackPayloadLightningJump? lightningJump,
  );

  StormTrackPayload nowcast(List<StormTrackPayloadNowcastInner>? nowcast);

  StormTrackPayload status(StormTrackPayloadStatusEnum status);

  StormTrackPayload mergedInto(String? mergedInto);

  StormTrackPayload parentTrackId(String? parentTrackId);

  StormTrackPayload detector(DetectorRef? detector);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  StormTrackPayload call({
    StormTrackPayloadCell cell,
    List<StormTrackPayloadHistoryInner>? history,
    StormTrackPayloadMotion motion,
    StormTrackPayloadTrend? trend,
    StormTrackPayloadLightningJump? lightningJump,
    List<StormTrackPayloadNowcastInner>? nowcast,
    StormTrackPayloadStatusEnum status,
    String? mergedInto,
    String? parentTrackId,
    DetectorRef? detector,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStormTrackPayload.copyWith(...)` or call `instanceOfStormTrackPayload.copyWith.fieldName(value)` for a single field.
class _$StormTrackPayloadCWProxyImpl implements _$StormTrackPayloadCWProxy {
  const _$StormTrackPayloadCWProxyImpl(this._value);

  final StormTrackPayload _value;

  @override
  StormTrackPayload cell(StormTrackPayloadCell cell) => call(cell: cell);

  @override
  StormTrackPayload history(List<StormTrackPayloadHistoryInner>? history) =>
      call(history: history);

  @override
  StormTrackPayload motion(StormTrackPayloadMotion motion) =>
      call(motion: motion);

  @override
  StormTrackPayload trend(StormTrackPayloadTrend? trend) => call(trend: trend);

  @override
  StormTrackPayload lightningJump(
    StormTrackPayloadLightningJump? lightningJump,
  ) => call(lightningJump: lightningJump);

  @override
  StormTrackPayload nowcast(List<StormTrackPayloadNowcastInner>? nowcast) =>
      call(nowcast: nowcast);

  @override
  StormTrackPayload status(StormTrackPayloadStatusEnum status) =>
      call(status: status);

  @override
  StormTrackPayload mergedInto(String? mergedInto) =>
      call(mergedInto: mergedInto);

  @override
  StormTrackPayload parentTrackId(String? parentTrackId) =>
      call(parentTrackId: parentTrackId);

  @override
  StormTrackPayload detector(DetectorRef? detector) => call(detector: detector);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StormTrackPayload call({
    Object? cell = const $CopyWithPlaceholder(),
    Object? history = const $CopyWithPlaceholder(),
    Object? motion = const $CopyWithPlaceholder(),
    Object? trend = const $CopyWithPlaceholder(),
    Object? lightningJump = const $CopyWithPlaceholder(),
    Object? nowcast = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? parentTrackId = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
  }) {
    return StormTrackPayload(
      cell: cell == const $CopyWithPlaceholder() || cell == null
          ? _value.cell
          // ignore: cast_nullable_to_non_nullable
          : cell as StormTrackPayloadCell,
      history: history == const $CopyWithPlaceholder()
          ? _value.history
          // ignore: cast_nullable_to_non_nullable
          : history as List<StormTrackPayloadHistoryInner>?,
      motion: motion == const $CopyWithPlaceholder() || motion == null
          ? _value.motion
          // ignore: cast_nullable_to_non_nullable
          : motion as StormTrackPayloadMotion,
      trend: trend == const $CopyWithPlaceholder()
          ? _value.trend
          // ignore: cast_nullable_to_non_nullable
          : trend as StormTrackPayloadTrend?,
      lightningJump: lightningJump == const $CopyWithPlaceholder()
          ? _value.lightningJump
          // ignore: cast_nullable_to_non_nullable
          : lightningJump as StormTrackPayloadLightningJump?,
      nowcast: nowcast == const $CopyWithPlaceholder()
          ? _value.nowcast
          // ignore: cast_nullable_to_non_nullable
          : nowcast as List<StormTrackPayloadNowcastInner>?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as StormTrackPayloadStatusEnum,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      parentTrackId: parentTrackId == const $CopyWithPlaceholder()
          ? _value.parentTrackId
          // ignore: cast_nullable_to_non_nullable
          : parentTrackId as String?,
      detector: detector == const $CopyWithPlaceholder()
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef?,
    );
  }
}

extension $StormTrackPayloadCopyWith on StormTrackPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStormTrackPayload.copyWith(...)` or `instanceOfStormTrackPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StormTrackPayloadCWProxy get copyWith =>
      _$StormTrackPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StormTrackPayload _$StormTrackPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StormTrackPayload', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['cell', 'motion', 'status']);
  final val = StormTrackPayload(
    cell: $checkedConvert(
      'cell',
      (v) => StormTrackPayloadCell.fromJson(v as Map<String, dynamic>),
    ),
    history: $checkedConvert(
      'history',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => StormTrackPayloadHistoryInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    motion: $checkedConvert(
      'motion',
      (v) => StormTrackPayloadMotion.fromJson(v as Map<String, dynamic>),
    ),
    trend: $checkedConvert(
      'trend',
      (v) => v == null
          ? null
          : StormTrackPayloadTrend.fromJson(v as Map<String, dynamic>),
    ),
    lightningJump: $checkedConvert(
      'lightningJump',
      (v) => v == null
          ? null
          : StormTrackPayloadLightningJump.fromJson(v as Map<String, dynamic>),
    ),
    nowcast: $checkedConvert(
      'nowcast',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => StormTrackPayloadNowcastInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$StormTrackPayloadStatusEnumEnumMap, v),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    parentTrackId: $checkedConvert('parentTrackId', (v) => v as String?),
    detector: $checkedConvert(
      'detector',
      (v) => v == null ? null : DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$StormTrackPayloadToJson(StormTrackPayload instance) =>
    <String, dynamic>{
      'cell': instance.cell.toJson(),
      'history': ?instance.history?.map((e) => e.toJson()).toList(),
      'motion': instance.motion.toJson(),
      'trend': ?instance.trend?.toJson(),
      'lightningJump': ?instance.lightningJump?.toJson(),
      'nowcast': ?instance.nowcast?.map((e) => e.toJson()).toList(),
      'status': _$StormTrackPayloadStatusEnumEnumMap[instance.status]!,
      'mergedInto': ?instance.mergedInto,
      'parentTrackId': ?instance.parentTrackId,
      'detector': ?instance.detector?.toJson(),
    };

const _$StormTrackPayloadStatusEnumEnumMap = {
  StormTrackPayloadStatusEnum.active: 'active',
  StormTrackPayloadStatusEnum.ended: 'ended',
};
