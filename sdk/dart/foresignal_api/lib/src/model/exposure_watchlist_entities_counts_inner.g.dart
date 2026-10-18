// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_watchlist_entities_counts_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposureWatchlistEntitiesCountsInnerCWProxy {
  ExposureWatchlistEntitiesCountsInner watchlistId(String watchlistId);

  ExposureWatchlistEntitiesCountsInner name(String? name);

  ExposureWatchlistEntitiesCountsInner count(int count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureWatchlistEntitiesCountsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureWatchlistEntitiesCountsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposureWatchlistEntitiesCountsInner call({
    String watchlistId,
    String? name,
    int count,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposureWatchlistEntitiesCountsInner.copyWith(...)` or call `instanceOfExposureWatchlistEntitiesCountsInner.copyWith.fieldName(value)` for a single field.
class _$ExposureWatchlistEntitiesCountsInnerCWProxyImpl
    implements _$ExposureWatchlistEntitiesCountsInnerCWProxy {
  const _$ExposureWatchlistEntitiesCountsInnerCWProxyImpl(this._value);

  final ExposureWatchlistEntitiesCountsInner _value;

  @override
  ExposureWatchlistEntitiesCountsInner watchlistId(String watchlistId) =>
      call(watchlistId: watchlistId);

  @override
  ExposureWatchlistEntitiesCountsInner name(String? name) => call(name: name);

  @override
  ExposureWatchlistEntitiesCountsInner count(int count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureWatchlistEntitiesCountsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureWatchlistEntitiesCountsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposureWatchlistEntitiesCountsInner call({
    Object? watchlistId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return ExposureWatchlistEntitiesCountsInner(
      watchlistId:
          watchlistId == const $CopyWithPlaceholder() || watchlistId == null
          ? _value.watchlistId
          // ignore: cast_nullable_to_non_nullable
          : watchlistId as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
    );
  }
}

extension $ExposureWatchlistEntitiesCountsInnerCopyWith
    on ExposureWatchlistEntitiesCountsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposureWatchlistEntitiesCountsInner.copyWith(...)` or `instanceOfExposureWatchlistEntitiesCountsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposureWatchlistEntitiesCountsInnerCWProxy get copyWith =>
      _$ExposureWatchlistEntitiesCountsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposureWatchlistEntitiesCountsInner
_$ExposureWatchlistEntitiesCountsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExposureWatchlistEntitiesCountsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['watchlistId', 'count']);
      final val = ExposureWatchlistEntitiesCountsInner(
        watchlistId: $checkedConvert('watchlistId', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String?),
        count: $checkedConvert('count', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ExposureWatchlistEntitiesCountsInnerToJson(
  ExposureWatchlistEntitiesCountsInner instance,
) => <String, dynamic>{
  'watchlistId': instance.watchlistId,
  'name': ?instance.name,
  'count': instance.count,
};
