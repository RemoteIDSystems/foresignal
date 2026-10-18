// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_watchlist_entities.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposureWatchlistEntitiesCWProxy {
  ExposureWatchlistEntities status(ExposureWatchlistEntitiesStatusEnum status);

  ExposureWatchlistEntities attribution(List<Attribution>? attribution);

  ExposureWatchlistEntities counts(
    List<ExposureWatchlistEntitiesCountsInner>? counts,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureWatchlistEntities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureWatchlistEntities(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposureWatchlistEntities call({
    ExposureWatchlistEntitiesStatusEnum status,
    List<Attribution>? attribution,
    List<ExposureWatchlistEntitiesCountsInner>? counts,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposureWatchlistEntities.copyWith(...)` or call `instanceOfExposureWatchlistEntities.copyWith.fieldName(value)` for a single field.
class _$ExposureWatchlistEntitiesCWProxyImpl
    implements _$ExposureWatchlistEntitiesCWProxy {
  const _$ExposureWatchlistEntitiesCWProxyImpl(this._value);

  final ExposureWatchlistEntities _value;

  @override
  ExposureWatchlistEntities status(
    ExposureWatchlistEntitiesStatusEnum status,
  ) => call(status: status);

  @override
  ExposureWatchlistEntities attribution(List<Attribution>? attribution) =>
      call(attribution: attribution);

  @override
  ExposureWatchlistEntities counts(
    List<ExposureWatchlistEntitiesCountsInner>? counts,
  ) => call(counts: counts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureWatchlistEntities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureWatchlistEntities(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposureWatchlistEntities call({
    Object? status = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? counts = const $CopyWithPlaceholder(),
  }) {
    return ExposureWatchlistEntities(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ExposureWatchlistEntitiesStatusEnum,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<Attribution>?,
      counts: counts == const $CopyWithPlaceholder()
          ? _value.counts
          // ignore: cast_nullable_to_non_nullable
          : counts as List<ExposureWatchlistEntitiesCountsInner>?,
    );
  }
}

extension $ExposureWatchlistEntitiesCopyWith on ExposureWatchlistEntities {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposureWatchlistEntities.copyWith(...)` or `instanceOfExposureWatchlistEntities.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposureWatchlistEntitiesCWProxy get copyWith =>
      _$ExposureWatchlistEntitiesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposureWatchlistEntities _$ExposureWatchlistEntitiesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExposureWatchlistEntities', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = ExposureWatchlistEntities(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ExposureWatchlistEntitiesStatusEnumEnumMap, v),
    ),
    attribution: $checkedConvert(
      'attribution',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    counts: $checkedConvert(
      'counts',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ExposureWatchlistEntitiesCountsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExposureWatchlistEntitiesToJson(
  ExposureWatchlistEntities instance,
) => <String, dynamic>{
  'status': _$ExposureWatchlistEntitiesStatusEnumEnumMap[instance.status]!,
  'attribution': ?instance.attribution?.map((e) => e.toJson()).toList(),
  'counts': ?instance.counts?.map((e) => e.toJson()).toList(),
};

const _$ExposureWatchlistEntitiesStatusEnumEnumMap = {
  ExposureWatchlistEntitiesStatusEnum.ok: 'ok',
  ExposureWatchlistEntitiesStatusEnum.unavailable: 'unavailable',
  ExposureWatchlistEntitiesStatusEnum.notInstalled: 'not-installed',
};
