// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rate_limit_settings_stream.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RateLimitSettingsStreamCWProxy {
  RateLimitSettingsStream opensPerMinute(int? opensPerMinute);

  RateLimitSettingsStream concurrent(int? concurrent);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RateLimitSettingsStream(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RateLimitSettingsStream(...).copyWith(id: 12, name: "My name")
  /// ```
  RateLimitSettingsStream call({int? opensPerMinute, int? concurrent});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRateLimitSettingsStream.copyWith(...)` or call `instanceOfRateLimitSettingsStream.copyWith.fieldName(value)` for a single field.
class _$RateLimitSettingsStreamCWProxyImpl
    implements _$RateLimitSettingsStreamCWProxy {
  const _$RateLimitSettingsStreamCWProxyImpl(this._value);

  final RateLimitSettingsStream _value;

  @override
  RateLimitSettingsStream opensPerMinute(int? opensPerMinute) =>
      call(opensPerMinute: opensPerMinute);

  @override
  RateLimitSettingsStream concurrent(int? concurrent) =>
      call(concurrent: concurrent);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RateLimitSettingsStream(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RateLimitSettingsStream(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RateLimitSettingsStream call({
    Object? opensPerMinute = const $CopyWithPlaceholder(),
    Object? concurrent = const $CopyWithPlaceholder(),
  }) {
    return RateLimitSettingsStream(
      opensPerMinute: opensPerMinute == const $CopyWithPlaceholder()
          ? _value.opensPerMinute
          // ignore: cast_nullable_to_non_nullable
          : opensPerMinute as int?,
      concurrent: concurrent == const $CopyWithPlaceholder()
          ? _value.concurrent
          // ignore: cast_nullable_to_non_nullable
          : concurrent as int?,
    );
  }
}

extension $RateLimitSettingsStreamCopyWith on RateLimitSettingsStream {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRateLimitSettingsStream.copyWith(...)` or `instanceOfRateLimitSettingsStream.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RateLimitSettingsStreamCWProxy get copyWith =>
      _$RateLimitSettingsStreamCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RateLimitSettingsStream _$RateLimitSettingsStreamFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RateLimitSettingsStream', json, ($checkedConvert) {
  final val = RateLimitSettingsStream(
    opensPerMinute: $checkedConvert(
      'opensPerMinute',
      (v) => (v as num?)?.toInt() ?? 30,
    ),
    concurrent: $checkedConvert('concurrent', (v) => (v as num?)?.toInt() ?? 8),
  );
  return val;
});

Map<String, dynamic> _$RateLimitSettingsStreamToJson(
  RateLimitSettingsStream instance,
) => <String, dynamic>{
  'opensPerMinute': ?instance.opensPerMinute,
  'concurrent': ?instance.concurrent,
};
