// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scorecard_entry_bias.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ScorecardEntryBiasCWProxy {
  ScorecardEntryBias meanForecastMinusObserved(num meanForecastMinusObserved);

  ScorecardEntryBias hedgedShare(num hedgedShare);

  ScorecardEntryBias meanForecastOnNo(num? meanForecastOnNo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntryBias(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntryBias(...).copyWith(id: 12, name: "My name")
  /// ```
  ScorecardEntryBias call({
    num meanForecastMinusObserved,
    num hedgedShare,
    num? meanForecastOnNo,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfScorecardEntryBias.copyWith(...)` or call `instanceOfScorecardEntryBias.copyWith.fieldName(value)` for a single field.
class _$ScorecardEntryBiasCWProxyImpl implements _$ScorecardEntryBiasCWProxy {
  const _$ScorecardEntryBiasCWProxyImpl(this._value);

  final ScorecardEntryBias _value;

  @override
  ScorecardEntryBias meanForecastMinusObserved(num meanForecastMinusObserved) =>
      call(meanForecastMinusObserved: meanForecastMinusObserved);

  @override
  ScorecardEntryBias hedgedShare(num hedgedShare) =>
      call(hedgedShare: hedgedShare);

  @override
  ScorecardEntryBias meanForecastOnNo(num? meanForecastOnNo) =>
      call(meanForecastOnNo: meanForecastOnNo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntryBias(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntryBias(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ScorecardEntryBias call({
    Object? meanForecastMinusObserved = const $CopyWithPlaceholder(),
    Object? hedgedShare = const $CopyWithPlaceholder(),
    Object? meanForecastOnNo = const $CopyWithPlaceholder(),
  }) {
    return ScorecardEntryBias(
      meanForecastMinusObserved:
          meanForecastMinusObserved == const $CopyWithPlaceholder() ||
              meanForecastMinusObserved == null
          ? _value.meanForecastMinusObserved
          // ignore: cast_nullable_to_non_nullable
          : meanForecastMinusObserved as num,
      hedgedShare:
          hedgedShare == const $CopyWithPlaceholder() || hedgedShare == null
          ? _value.hedgedShare
          // ignore: cast_nullable_to_non_nullable
          : hedgedShare as num,
      meanForecastOnNo: meanForecastOnNo == const $CopyWithPlaceholder()
          ? _value.meanForecastOnNo
          // ignore: cast_nullable_to_non_nullable
          : meanForecastOnNo as num?,
    );
  }
}

extension $ScorecardEntryBiasCopyWith on ScorecardEntryBias {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfScorecardEntryBias.copyWith(...)` or `instanceOfScorecardEntryBias.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ScorecardEntryBiasCWProxy get copyWith =>
      _$ScorecardEntryBiasCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScorecardEntryBias _$ScorecardEntryBiasFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ScorecardEntryBias', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['meanForecastMinusObserved', 'hedgedShare'],
      );
      final val = ScorecardEntryBias(
        meanForecastMinusObserved: $checkedConvert(
          'meanForecastMinusObserved',
          (v) => v as num,
        ),
        hedgedShare: $checkedConvert('hedgedShare', (v) => v as num),
        meanForecastOnNo: $checkedConvert('meanForecastOnNo', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$ScorecardEntryBiasToJson(ScorecardEntryBias instance) =>
    <String, dynamic>{
      'meanForecastMinusObserved': instance.meanForecastMinusObserved,
      'hedgedShare': instance.hedgedShare,
      'meanForecastOnNo': ?instance.meanForecastOnNo,
    };
