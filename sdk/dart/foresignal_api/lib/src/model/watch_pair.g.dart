// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watch_pair.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchPairCWProxy {
  WatchPair eventFamily(String eventFamily);

  WatchPair regionCodes(List<String>? regionCodes);

  WatchPair selector(EventSelector? selector);

  WatchPair minEffect(num? minEffect);

  WatchPair releaseLagDays(int? releaseLagDays);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchPair(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchPair(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchPair call({
    String eventFamily,
    List<String>? regionCodes,
    EventSelector? selector,
    num? minEffect,
    int? releaseLagDays,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchPair.copyWith(...)` or call `instanceOfWatchPair.copyWith.fieldName(value)` for a single field.
class _$WatchPairCWProxyImpl implements _$WatchPairCWProxy {
  const _$WatchPairCWProxyImpl(this._value);

  final WatchPair _value;

  @override
  WatchPair eventFamily(String eventFamily) => call(eventFamily: eventFamily);

  @override
  WatchPair regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  WatchPair selector(EventSelector? selector) => call(selector: selector);

  @override
  WatchPair minEffect(num? minEffect) => call(minEffect: minEffect);

  @override
  WatchPair releaseLagDays(int? releaseLagDays) =>
      call(releaseLagDays: releaseLagDays);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchPair(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchPair(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchPair call({
    Object? eventFamily = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? selector = const $CopyWithPlaceholder(),
    Object? minEffect = const $CopyWithPlaceholder(),
    Object? releaseLagDays = const $CopyWithPlaceholder(),
  }) {
    return WatchPair(
      eventFamily:
          eventFamily == const $CopyWithPlaceholder() || eventFamily == null
          ? _value.eventFamily
          // ignore: cast_nullable_to_non_nullable
          : eventFamily as String,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      selector: selector == const $CopyWithPlaceholder()
          ? _value.selector
          // ignore: cast_nullable_to_non_nullable
          : selector as EventSelector?,
      minEffect: minEffect == const $CopyWithPlaceholder()
          ? _value.minEffect
          // ignore: cast_nullable_to_non_nullable
          : minEffect as num?,
      releaseLagDays: releaseLagDays == const $CopyWithPlaceholder()
          ? _value.releaseLagDays
          // ignore: cast_nullable_to_non_nullable
          : releaseLagDays as int?,
    );
  }
}

extension $WatchPairCopyWith on WatchPair {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchPair.copyWith(...)` or `instanceOfWatchPair.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchPairCWProxy get copyWith => _$WatchPairCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchPair _$WatchPairFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WatchPair', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['eventFamily']);
      final val = WatchPair(
        eventFamily: $checkedConvert('eventFamily', (v) => v as String),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        selector: $checkedConvert(
          'selector',
          (v) => v == null
              ? null
              : EventSelector.fromJson(v as Map<String, dynamic>),
        ),
        minEffect: $checkedConvert('minEffect', (v) => v as num? ?? 2.0),
        releaseLagDays: $checkedConvert(
          'releaseLagDays',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$WatchPairToJson(WatchPair instance) => <String, dynamic>{
  'eventFamily': instance.eventFamily,
  'regionCodes': ?instance.regionCodes,
  'selector': ?instance.selector?.toJson(),
  'minEffect': ?instance.minEffect,
  'releaseLagDays': ?instance.releaseLagDays,
};
