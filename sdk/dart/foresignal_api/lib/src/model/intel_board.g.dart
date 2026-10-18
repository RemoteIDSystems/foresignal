// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardCWProxy {
  IntelBoard generatedAt(DateTime generatedAt);

  IntelBoard situations(List<IntelBoardSituationsInner> situations);

  IntelBoard bandChanges(List<IntelBoardBandChangesInner> bandChanges);

  IntelBoard indicatorsObserved(
    List<IntelBoardIndicatorsObservedInner> indicatorsObserved,
  );

  IntelBoard forecastsDue(List<IntelBoardForecastsDueInner> forecastsDue);

  IntelBoard latestAssessments(
    List<IntelBoardLatestAssessmentsInner> latestAssessments,
  );

  IntelBoard calibration(IntelBoardCalibration? calibration);

  IntelBoard benchmarks(List<IntelBoardBenchmarksInner>? benchmarks);

  IntelBoard truncated(List<String>? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoard(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoard(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoard call({
    DateTime generatedAt,
    List<IntelBoardSituationsInner> situations,
    List<IntelBoardBandChangesInner> bandChanges,
    List<IntelBoardIndicatorsObservedInner> indicatorsObserved,
    List<IntelBoardForecastsDueInner> forecastsDue,
    List<IntelBoardLatestAssessmentsInner> latestAssessments,
    IntelBoardCalibration? calibration,
    List<IntelBoardBenchmarksInner>? benchmarks,
    List<String>? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoard.copyWith(...)` or call `instanceOfIntelBoard.copyWith.fieldName(value)` for a single field.
class _$IntelBoardCWProxyImpl implements _$IntelBoardCWProxy {
  const _$IntelBoardCWProxyImpl(this._value);

  final IntelBoard _value;

  @override
  IntelBoard generatedAt(DateTime generatedAt) =>
      call(generatedAt: generatedAt);

  @override
  IntelBoard situations(List<IntelBoardSituationsInner> situations) =>
      call(situations: situations);

  @override
  IntelBoard bandChanges(List<IntelBoardBandChangesInner> bandChanges) =>
      call(bandChanges: bandChanges);

  @override
  IntelBoard indicatorsObserved(
    List<IntelBoardIndicatorsObservedInner> indicatorsObserved,
  ) => call(indicatorsObserved: indicatorsObserved);

  @override
  IntelBoard forecastsDue(List<IntelBoardForecastsDueInner> forecastsDue) =>
      call(forecastsDue: forecastsDue);

  @override
  IntelBoard latestAssessments(
    List<IntelBoardLatestAssessmentsInner> latestAssessments,
  ) => call(latestAssessments: latestAssessments);

  @override
  IntelBoard calibration(IntelBoardCalibration? calibration) =>
      call(calibration: calibration);

  @override
  IntelBoard benchmarks(List<IntelBoardBenchmarksInner>? benchmarks) =>
      call(benchmarks: benchmarks);

  @override
  IntelBoard truncated(List<String>? truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoard(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoard(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoard call({
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? situations = const $CopyWithPlaceholder(),
    Object? bandChanges = const $CopyWithPlaceholder(),
    Object? indicatorsObserved = const $CopyWithPlaceholder(),
    Object? forecastsDue = const $CopyWithPlaceholder(),
    Object? latestAssessments = const $CopyWithPlaceholder(),
    Object? calibration = const $CopyWithPlaceholder(),
    Object? benchmarks = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return IntelBoard(
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
      situations:
          situations == const $CopyWithPlaceholder() || situations == null
          ? _value.situations
          // ignore: cast_nullable_to_non_nullable
          : situations as List<IntelBoardSituationsInner>,
      bandChanges:
          bandChanges == const $CopyWithPlaceholder() || bandChanges == null
          ? _value.bandChanges
          // ignore: cast_nullable_to_non_nullable
          : bandChanges as List<IntelBoardBandChangesInner>,
      indicatorsObserved:
          indicatorsObserved == const $CopyWithPlaceholder() ||
              indicatorsObserved == null
          ? _value.indicatorsObserved
          // ignore: cast_nullable_to_non_nullable
          : indicatorsObserved as List<IntelBoardIndicatorsObservedInner>,
      forecastsDue:
          forecastsDue == const $CopyWithPlaceholder() || forecastsDue == null
          ? _value.forecastsDue
          // ignore: cast_nullable_to_non_nullable
          : forecastsDue as List<IntelBoardForecastsDueInner>,
      latestAssessments:
          latestAssessments == const $CopyWithPlaceholder() ||
              latestAssessments == null
          ? _value.latestAssessments
          // ignore: cast_nullable_to_non_nullable
          : latestAssessments as List<IntelBoardLatestAssessmentsInner>,
      calibration: calibration == const $CopyWithPlaceholder()
          ? _value.calibration
          // ignore: cast_nullable_to_non_nullable
          : calibration as IntelBoardCalibration?,
      benchmarks: benchmarks == const $CopyWithPlaceholder()
          ? _value.benchmarks
          // ignore: cast_nullable_to_non_nullable
          : benchmarks as List<IntelBoardBenchmarksInner>?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as List<String>?,
    );
  }
}

extension $IntelBoardCopyWith on IntelBoard {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoard.copyWith(...)` or `instanceOfIntelBoard.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardCWProxy get copyWith => _$IntelBoardCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoard _$IntelBoardFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoard', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'generatedAt',
      'situations',
      'bandChanges',
      'indicatorsObserved',
      'forecastsDue',
      'latestAssessments',
    ],
  );
  final val = IntelBoard(
    generatedAt: $checkedConvert(
      'generatedAt',
      (v) => DateTime.parse(v as String),
    ),
    situations: $checkedConvert(
      'situations',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                IntelBoardSituationsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    bandChanges: $checkedConvert(
      'bandChanges',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                IntelBoardBandChangesInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    indicatorsObserved: $checkedConvert(
      'indicatorsObserved',
      (v) => (v as List<dynamic>)
          .map(
            (e) => IntelBoardIndicatorsObservedInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    forecastsDue: $checkedConvert(
      'forecastsDue',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                IntelBoardForecastsDueInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    latestAssessments: $checkedConvert(
      'latestAssessments',
      (v) => (v as List<dynamic>)
          .map(
            (e) => IntelBoardLatestAssessmentsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    calibration: $checkedConvert(
      'calibration',
      (v) => v == null
          ? null
          : IntelBoardCalibration.fromJson(v as Map<String, dynamic>),
    ),
    benchmarks: $checkedConvert(
      'benchmarks',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                IntelBoardBenchmarksInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    truncated: $checkedConvert(
      'truncated',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelBoardToJson(IntelBoard instance) =>
    <String, dynamic>{
      'generatedAt': instance.generatedAt.toIso8601String(),
      'situations': instance.situations.map((e) => e.toJson()).toList(),
      'bandChanges': instance.bandChanges.map((e) => e.toJson()).toList(),
      'indicatorsObserved': instance.indicatorsObserved
          .map((e) => e.toJson())
          .toList(),
      'forecastsDue': instance.forecastsDue.map((e) => e.toJson()).toList(),
      'latestAssessments': instance.latestAssessments
          .map((e) => e.toJson())
          .toList(),
      'calibration': ?instance.calibration?.toJson(),
      'benchmarks': ?instance.benchmarks?.map((e) => e.toJson()).toList(),
      'truncated': ?instance.truncated,
    };
