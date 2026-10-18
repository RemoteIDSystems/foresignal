// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'json_feed_item_foresignal.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$JsonFeedItemForesignalCWProxy {
  JsonFeedItemForesignal severity(JsonFeedItemForesignalSeverityEnum? severity);

  JsonFeedItemForesignal state(JsonFeedItemForesignalStateEnum? state);

  JsonFeedItemForesignal sourceId(String? sourceId);

  JsonFeedItemForesignal externalId(String? externalId);

  JsonFeedItemForesignal geometry(Geometry? geometry);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeedItemForesignal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeedItemForesignal(...).copyWith(id: 12, name: "My name")
  /// ```
  JsonFeedItemForesignal call({
    JsonFeedItemForesignalSeverityEnum? severity,
    JsonFeedItemForesignalStateEnum? state,
    String? sourceId,
    String? externalId,
    Geometry? geometry,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfJsonFeedItemForesignal.copyWith(...)` or call `instanceOfJsonFeedItemForesignal.copyWith.fieldName(value)` for a single field.
class _$JsonFeedItemForesignalCWProxyImpl
    implements _$JsonFeedItemForesignalCWProxy {
  const _$JsonFeedItemForesignalCWProxyImpl(this._value);

  final JsonFeedItemForesignal _value;

  @override
  JsonFeedItemForesignal severity(
    JsonFeedItemForesignalSeverityEnum? severity,
  ) => call(severity: severity);

  @override
  JsonFeedItemForesignal state(JsonFeedItemForesignalStateEnum? state) =>
      call(state: state);

  @override
  JsonFeedItemForesignal sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  JsonFeedItemForesignal externalId(String? externalId) =>
      call(externalId: externalId);

  @override
  JsonFeedItemForesignal geometry(Geometry? geometry) =>
      call(geometry: geometry);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeedItemForesignal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeedItemForesignal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  JsonFeedItemForesignal call({
    Object? severity = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
  }) {
    return JsonFeedItemForesignal(
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as JsonFeedItemForesignalSeverityEnum?,
      state: state == const $CopyWithPlaceholder()
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as JsonFeedItemForesignalStateEnum?,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
    );
  }
}

extension $JsonFeedItemForesignalCopyWith on JsonFeedItemForesignal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfJsonFeedItemForesignal.copyWith(...)` or `instanceOfJsonFeedItemForesignal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$JsonFeedItemForesignalCWProxy get copyWith =>
      _$JsonFeedItemForesignalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

JsonFeedItemForesignal _$JsonFeedItemForesignalFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('JsonFeedItemForesignal', json, ($checkedConvert) {
  final val = JsonFeedItemForesignal(
    severity: $checkedConvert(
      'severity',
      (v) =>
          $enumDecodeNullable(_$JsonFeedItemForesignalSeverityEnumEnumMap, v),
    ),
    state: $checkedConvert(
      'state',
      (v) => $enumDecodeNullable(_$JsonFeedItemForesignalStateEnumEnumMap, v),
    ),
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
    externalId: $checkedConvert('externalId', (v) => v as String?),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$JsonFeedItemForesignalToJson(
  JsonFeedItemForesignal instance,
) => <String, dynamic>{
  'severity': ?_$JsonFeedItemForesignalSeverityEnumEnumMap[instance.severity],
  'state': ?_$JsonFeedItemForesignalStateEnumEnumMap[instance.state],
  'sourceId': ?instance.sourceId,
  'externalId': ?instance.externalId,
  'geometry': ?instance.geometry?.toJson(),
};

const _$JsonFeedItemForesignalSeverityEnumEnumMap = {
  JsonFeedItemForesignalSeverityEnum.info: 'info',
  JsonFeedItemForesignalSeverityEnum.warning: 'warning',
  JsonFeedItemForesignalSeverityEnum.critical: 'critical',
};

const _$JsonFeedItemForesignalStateEnumEnumMap = {
  JsonFeedItemForesignalStateEnum.firing: 'firing',
  JsonFeedItemForesignalStateEnum.acknowledged: 'acknowledged',
  JsonFeedItemForesignalStateEnum.resolved: 'resolved',
};
