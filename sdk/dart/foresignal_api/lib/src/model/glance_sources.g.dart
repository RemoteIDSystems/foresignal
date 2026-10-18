// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'glance_sources.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GlanceSourcesCWProxy {
  GlanceSources healthy(int? healthy);

  GlanceSources degraded(int? degraded);

  GlanceSources failing(int? failing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceSources(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceSources(...).copyWith(id: 12, name: "My name")
  /// ```
  GlanceSources call({int? healthy, int? degraded, int? failing});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGlanceSources.copyWith(...)` or call `instanceOfGlanceSources.copyWith.fieldName(value)` for a single field.
class _$GlanceSourcesCWProxyImpl implements _$GlanceSourcesCWProxy {
  const _$GlanceSourcesCWProxyImpl(this._value);

  final GlanceSources _value;

  @override
  GlanceSources healthy(int? healthy) => call(healthy: healthy);

  @override
  GlanceSources degraded(int? degraded) => call(degraded: degraded);

  @override
  GlanceSources failing(int? failing) => call(failing: failing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceSources(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceSources(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GlanceSources call({
    Object? healthy = const $CopyWithPlaceholder(),
    Object? degraded = const $CopyWithPlaceholder(),
    Object? failing = const $CopyWithPlaceholder(),
  }) {
    return GlanceSources(
      healthy: healthy == const $CopyWithPlaceholder()
          ? _value.healthy
          // ignore: cast_nullable_to_non_nullable
          : healthy as int?,
      degraded: degraded == const $CopyWithPlaceholder()
          ? _value.degraded
          // ignore: cast_nullable_to_non_nullable
          : degraded as int?,
      failing: failing == const $CopyWithPlaceholder()
          ? _value.failing
          // ignore: cast_nullable_to_non_nullable
          : failing as int?,
    );
  }
}

extension $GlanceSourcesCopyWith on GlanceSources {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGlanceSources.copyWith(...)` or `instanceOfGlanceSources.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GlanceSourcesCWProxy get copyWith => _$GlanceSourcesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GlanceSources _$GlanceSourcesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GlanceSources', json, ($checkedConvert) {
      final val = GlanceSources(
        healthy: $checkedConvert('healthy', (v) => (v as num?)?.toInt()),
        degraded: $checkedConvert('degraded', (v) => (v as num?)?.toInt()),
        failing: $checkedConvert('failing', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$GlanceSourcesToJson(GlanceSources instance) =>
    <String, dynamic>{
      'healthy': ?instance.healthy,
      'degraded': ?instance.degraded,
      'failing': ?instance.failing,
    };
