// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_spend_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmSpendWindowCWProxy {
  LlmSpendWindow window(LlmSpendWindowWindowEnum window);

  LlmSpendWindow start(DateTime start);

  LlmSpendWindow resetsAt(DateTime resetsAt);

  LlmSpendWindow spentUsd(num spentUsd);

  LlmSpendWindow reservedUsd(num reservedUsd);

  LlmSpendWindow capUsd(num? capUsd);

  LlmSpendWindow remainingUsd(num? remainingUsd);

  LlmSpendWindow exhausted(bool exhausted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmSpendWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmSpendWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmSpendWindow call({
    LlmSpendWindowWindowEnum window,
    DateTime start,
    DateTime resetsAt,
    num spentUsd,
    num reservedUsd,
    num? capUsd,
    num? remainingUsd,
    bool exhausted,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmSpendWindow.copyWith(...)` or call `instanceOfLlmSpendWindow.copyWith.fieldName(value)` for a single field.
class _$LlmSpendWindowCWProxyImpl implements _$LlmSpendWindowCWProxy {
  const _$LlmSpendWindowCWProxyImpl(this._value);

  final LlmSpendWindow _value;

  @override
  LlmSpendWindow window(LlmSpendWindowWindowEnum window) =>
      call(window: window);

  @override
  LlmSpendWindow start(DateTime start) => call(start: start);

  @override
  LlmSpendWindow resetsAt(DateTime resetsAt) => call(resetsAt: resetsAt);

  @override
  LlmSpendWindow spentUsd(num spentUsd) => call(spentUsd: spentUsd);

  @override
  LlmSpendWindow reservedUsd(num reservedUsd) => call(reservedUsd: reservedUsd);

  @override
  LlmSpendWindow capUsd(num? capUsd) => call(capUsd: capUsd);

  @override
  LlmSpendWindow remainingUsd(num? remainingUsd) =>
      call(remainingUsd: remainingUsd);

  @override
  LlmSpendWindow exhausted(bool exhausted) => call(exhausted: exhausted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmSpendWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmSpendWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmSpendWindow call({
    Object? window = const $CopyWithPlaceholder(),
    Object? start = const $CopyWithPlaceholder(),
    Object? resetsAt = const $CopyWithPlaceholder(),
    Object? spentUsd = const $CopyWithPlaceholder(),
    Object? reservedUsd = const $CopyWithPlaceholder(),
    Object? capUsd = const $CopyWithPlaceholder(),
    Object? remainingUsd = const $CopyWithPlaceholder(),
    Object? exhausted = const $CopyWithPlaceholder(),
  }) {
    return LlmSpendWindow(
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as LlmSpendWindowWindowEnum,
      start: start == const $CopyWithPlaceholder() || start == null
          ? _value.start
          // ignore: cast_nullable_to_non_nullable
          : start as DateTime,
      resetsAt: resetsAt == const $CopyWithPlaceholder() || resetsAt == null
          ? _value.resetsAt
          // ignore: cast_nullable_to_non_nullable
          : resetsAt as DateTime,
      spentUsd: spentUsd == const $CopyWithPlaceholder() || spentUsd == null
          ? _value.spentUsd
          // ignore: cast_nullable_to_non_nullable
          : spentUsd as num,
      reservedUsd:
          reservedUsd == const $CopyWithPlaceholder() || reservedUsd == null
          ? _value.reservedUsd
          // ignore: cast_nullable_to_non_nullable
          : reservedUsd as num,
      capUsd: capUsd == const $CopyWithPlaceholder()
          ? _value.capUsd
          // ignore: cast_nullable_to_non_nullable
          : capUsd as num?,
      remainingUsd: remainingUsd == const $CopyWithPlaceholder()
          ? _value.remainingUsd
          // ignore: cast_nullable_to_non_nullable
          : remainingUsd as num?,
      exhausted: exhausted == const $CopyWithPlaceholder() || exhausted == null
          ? _value.exhausted
          // ignore: cast_nullable_to_non_nullable
          : exhausted as bool,
    );
  }
}

extension $LlmSpendWindowCopyWith on LlmSpendWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmSpendWindow.copyWith(...)` or `instanceOfLlmSpendWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmSpendWindowCWProxy get copyWith => _$LlmSpendWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmSpendWindow _$LlmSpendWindowFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmSpendWindow', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'window',
          'start',
          'resetsAt',
          'spentUsd',
          'reservedUsd',
          'capUsd',
          'remainingUsd',
          'exhausted',
        ],
      );
      final val = LlmSpendWindow(
        window: $checkedConvert(
          'window',
          (v) => $enumDecode(_$LlmSpendWindowWindowEnumEnumMap, v),
        ),
        start: $checkedConvert('start', (v) => DateTime.parse(v as String)),
        resetsAt: $checkedConvert(
          'resetsAt',
          (v) => DateTime.parse(v as String),
        ),
        spentUsd: $checkedConvert('spentUsd', (v) => v as num),
        reservedUsd: $checkedConvert('reservedUsd', (v) => v as num),
        capUsd: $checkedConvert('capUsd', (v) => v as num?),
        remainingUsd: $checkedConvert('remainingUsd', (v) => v as num?),
        exhausted: $checkedConvert('exhausted', (v) => v as bool),
      );
      return val;
    });

Map<String, dynamic> _$LlmSpendWindowToJson(LlmSpendWindow instance) =>
    <String, dynamic>{
      'window': _$LlmSpendWindowWindowEnumEnumMap[instance.window]!,
      'start': instance.start.toIso8601String(),
      'resetsAt': instance.resetsAt.toIso8601String(),
      'spentUsd': instance.spentUsd,
      'reservedUsd': instance.reservedUsd,
      'capUsd': instance.capUsd,
      'remainingUsd': instance.remainingUsd,
      'exhausted': instance.exhausted,
    };

const _$LlmSpendWindowWindowEnumEnumMap = {
  LlmSpendWindowWindowEnum.hour: 'hour',
  LlmSpendWindowWindowEnum.day: 'day',
  LlmSpendWindowWindowEnum.month: 'month',
};
