// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'frame_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FrameStatsCWProxy {
  FrameStats min(num? min);

  FrameStats max(num? max);

  FrameStats mean(num? mean);

  FrameStats coveragePct(num? coveragePct);

  FrameStats codeCounts(Map<String, int>? codeCounts);

  FrameStats flagMissing(bool? flagMissing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FrameStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FrameStats(...).copyWith(id: 12, name: "My name")
  /// ```
  FrameStats call({
    num? min,
    num? max,
    num? mean,
    num? coveragePct,
    Map<String, int>? codeCounts,
    bool? flagMissing,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFrameStats.copyWith(...)` or call `instanceOfFrameStats.copyWith.fieldName(value)` for a single field.
class _$FrameStatsCWProxyImpl implements _$FrameStatsCWProxy {
  const _$FrameStatsCWProxyImpl(this._value);

  final FrameStats _value;

  @override
  FrameStats min(num? min) => call(min: min);

  @override
  FrameStats max(num? max) => call(max: max);

  @override
  FrameStats mean(num? mean) => call(mean: mean);

  @override
  FrameStats coveragePct(num? coveragePct) => call(coveragePct: coveragePct);

  @override
  FrameStats codeCounts(Map<String, int>? codeCounts) =>
      call(codeCounts: codeCounts);

  @override
  FrameStats flagMissing(bool? flagMissing) => call(flagMissing: flagMissing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FrameStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FrameStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FrameStats call({
    Object? min = const $CopyWithPlaceholder(),
    Object? max = const $CopyWithPlaceholder(),
    Object? mean = const $CopyWithPlaceholder(),
    Object? coveragePct = const $CopyWithPlaceholder(),
    Object? codeCounts = const $CopyWithPlaceholder(),
    Object? flagMissing = const $CopyWithPlaceholder(),
  }) {
    return FrameStats(
      min: min == const $CopyWithPlaceholder()
          ? _value.min
          // ignore: cast_nullable_to_non_nullable
          : min as num?,
      max: max == const $CopyWithPlaceholder()
          ? _value.max
          // ignore: cast_nullable_to_non_nullable
          : max as num?,
      mean: mean == const $CopyWithPlaceholder()
          ? _value.mean
          // ignore: cast_nullable_to_non_nullable
          : mean as num?,
      coveragePct: coveragePct == const $CopyWithPlaceholder()
          ? _value.coveragePct
          // ignore: cast_nullable_to_non_nullable
          : coveragePct as num?,
      codeCounts: codeCounts == const $CopyWithPlaceholder()
          ? _value.codeCounts
          // ignore: cast_nullable_to_non_nullable
          : codeCounts as Map<String, int>?,
      flagMissing: flagMissing == const $CopyWithPlaceholder()
          ? _value.flagMissing
          // ignore: cast_nullable_to_non_nullable
          : flagMissing as bool?,
    );
  }
}

extension $FrameStatsCopyWith on FrameStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFrameStats.copyWith(...)` or `instanceOfFrameStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FrameStatsCWProxy get copyWith => _$FrameStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FrameStats _$FrameStatsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FrameStats', json, ($checkedConvert) {
      final val = FrameStats(
        min: $checkedConvert('min', (v) => v as num?),
        max: $checkedConvert('max', (v) => v as num?),
        mean: $checkedConvert('mean', (v) => v as num?),
        coveragePct: $checkedConvert('coveragePct', (v) => v as num?),
        codeCounts: $checkedConvert(
          'codeCounts',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        flagMissing: $checkedConvert('flagMissing', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$FrameStatsToJson(FrameStats instance) =>
    <String, dynamic>{
      'min': ?instance.min,
      'max': ?instance.max,
      'mean': ?instance.mean,
      'coveragePct': ?instance.coveragePct,
      'codeCounts': ?instance.codeCounts,
      'flagMissing': ?instance.flagMissing,
    };
