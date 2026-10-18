// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rate_limit_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RateLimitSettingsCWProxy {
  RateLimitSettings read(int? read);

  RateLimitSettings tiles(int? tiles);

  RateLimitSettings stream(RateLimitSettingsStream? stream);

  RateLimitSettings expensive(int? expensive);

  RateLimitSettings write(int? write);

  RateLimitSettings anonymous(int? anonymous);

  RateLimitSettings metricsPerMinute(int? metricsPerMinute);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RateLimitSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RateLimitSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  RateLimitSettings call({
    int? read,
    int? tiles,
    RateLimitSettingsStream? stream,
    int? expensive,
    int? write,
    int? anonymous,
    int? metricsPerMinute,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRateLimitSettings.copyWith(...)` or call `instanceOfRateLimitSettings.copyWith.fieldName(value)` for a single field.
class _$RateLimitSettingsCWProxyImpl implements _$RateLimitSettingsCWProxy {
  const _$RateLimitSettingsCWProxyImpl(this._value);

  final RateLimitSettings _value;

  @override
  RateLimitSettings read(int? read) => call(read: read);

  @override
  RateLimitSettings tiles(int? tiles) => call(tiles: tiles);

  @override
  RateLimitSettings stream(RateLimitSettingsStream? stream) =>
      call(stream: stream);

  @override
  RateLimitSettings expensive(int? expensive) => call(expensive: expensive);

  @override
  RateLimitSettings write(int? write) => call(write: write);

  @override
  RateLimitSettings anonymous(int? anonymous) => call(anonymous: anonymous);

  @override
  RateLimitSettings metricsPerMinute(int? metricsPerMinute) =>
      call(metricsPerMinute: metricsPerMinute);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RateLimitSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RateLimitSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RateLimitSettings call({
    Object? read = const $CopyWithPlaceholder(),
    Object? tiles = const $CopyWithPlaceholder(),
    Object? stream = const $CopyWithPlaceholder(),
    Object? expensive = const $CopyWithPlaceholder(),
    Object? write = const $CopyWithPlaceholder(),
    Object? anonymous = const $CopyWithPlaceholder(),
    Object? metricsPerMinute = const $CopyWithPlaceholder(),
  }) {
    return RateLimitSettings(
      read: read == const $CopyWithPlaceholder()
          ? _value.read
          // ignore: cast_nullable_to_non_nullable
          : read as int?,
      tiles: tiles == const $CopyWithPlaceholder()
          ? _value.tiles
          // ignore: cast_nullable_to_non_nullable
          : tiles as int?,
      stream: stream == const $CopyWithPlaceholder()
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as RateLimitSettingsStream?,
      expensive: expensive == const $CopyWithPlaceholder()
          ? _value.expensive
          // ignore: cast_nullable_to_non_nullable
          : expensive as int?,
      write: write == const $CopyWithPlaceholder()
          ? _value.write
          // ignore: cast_nullable_to_non_nullable
          : write as int?,
      anonymous: anonymous == const $CopyWithPlaceholder()
          ? _value.anonymous
          // ignore: cast_nullable_to_non_nullable
          : anonymous as int?,
      metricsPerMinute: metricsPerMinute == const $CopyWithPlaceholder()
          ? _value.metricsPerMinute
          // ignore: cast_nullable_to_non_nullable
          : metricsPerMinute as int?,
    );
  }
}

extension $RateLimitSettingsCopyWith on RateLimitSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRateLimitSettings.copyWith(...)` or `instanceOfRateLimitSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RateLimitSettingsCWProxy get copyWith =>
      _$RateLimitSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RateLimitSettings _$RateLimitSettingsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RateLimitSettings', json, ($checkedConvert) {
  final val = RateLimitSettings(
    read: $checkedConvert('read', (v) => (v as num?)?.toInt() ?? 600),
    tiles: $checkedConvert('tiles', (v) => (v as num?)?.toInt() ?? 1200),
    stream: $checkedConvert(
      'stream',
      (v) => v == null
          ? null
          : RateLimitSettingsStream.fromJson(v as Map<String, dynamic>),
    ),
    expensive: $checkedConvert('expensive', (v) => (v as num?)?.toInt() ?? 20),
    write: $checkedConvert('write', (v) => (v as num?)?.toInt() ?? 120),
    anonymous: $checkedConvert('anonymous', (v) => (v as num?)?.toInt() ?? 120),
    metricsPerMinute: $checkedConvert(
      'metricsPerMinute',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$RateLimitSettingsToJson(RateLimitSettings instance) =>
    <String, dynamic>{
      'read': ?instance.read,
      'tiles': ?instance.tiles,
      'stream': ?instance.stream?.toJson(),
      'expensive': ?instance.expensive,
      'write': ?instance.write,
      'anonymous': ?instance.anonymous,
      'metricsPerMinute': ?instance.metricsPerMinute,
    };
