// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'capture_mode.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CaptureModeCWProxy {
  CaptureMode mode(CaptureModeModeEnum mode);

  CaptureMode interval(String? interval);

  CaptureMode everyNthFrame(int? everyNthFrame);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CaptureMode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CaptureMode(...).copyWith(id: 12, name: "My name")
  /// ```
  CaptureMode call({
    CaptureModeModeEnum mode,
    String? interval,
    int? everyNthFrame,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCaptureMode.copyWith(...)` or call `instanceOfCaptureMode.copyWith.fieldName(value)` for a single field.
class _$CaptureModeCWProxyImpl implements _$CaptureModeCWProxy {
  const _$CaptureModeCWProxyImpl(this._value);

  final CaptureMode _value;

  @override
  CaptureMode mode(CaptureModeModeEnum mode) => call(mode: mode);

  @override
  CaptureMode interval(String? interval) => call(interval: interval);

  @override
  CaptureMode everyNthFrame(int? everyNthFrame) =>
      call(everyNthFrame: everyNthFrame);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CaptureMode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CaptureMode(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CaptureMode call({
    Object? mode = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? everyNthFrame = const $CopyWithPlaceholder(),
  }) {
    return CaptureMode(
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as CaptureModeModeEnum,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as String?,
      everyNthFrame: everyNthFrame == const $CopyWithPlaceholder()
          ? _value.everyNthFrame
          // ignore: cast_nullable_to_non_nullable
          : everyNthFrame as int?,
    );
  }
}

extension $CaptureModeCopyWith on CaptureMode {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCaptureMode.copyWith(...)` or `instanceOfCaptureMode.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CaptureModeCWProxy get copyWith => _$CaptureModeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CaptureMode _$CaptureModeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CaptureMode', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['mode']);
      final val = CaptureMode(
        mode: $checkedConvert(
          'mode',
          (v) => $enumDecode(_$CaptureModeModeEnumEnumMap, v),
        ),
        interval: $checkedConvert('interval', (v) => v as String?),
        everyNthFrame: $checkedConvert(
          'everyNthFrame',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CaptureModeToJson(CaptureMode instance) =>
    <String, dynamic>{
      'mode': _$CaptureModeModeEnumEnumMap[instance.mode]!,
      'interval': ?instance.interval,
      'everyNthFrame': ?instance.everyNthFrame,
    };

const _$CaptureModeModeEnumEnumMap = {
  CaptureModeModeEnum.full: 'full',
  CaptureModeModeEnum.downsampled: 'downsampled',
  CaptureModeModeEnum.eventsOnly: 'events-only',
  CaptureModeModeEnum.latestPerInterval: 'latest-per-interval',
  CaptureModeModeEnum.rollupOnly: 'rollup-only',
  CaptureModeModeEnum.frames: 'frames',
};
