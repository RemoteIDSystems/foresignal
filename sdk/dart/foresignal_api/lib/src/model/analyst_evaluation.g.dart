// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_evaluation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystEvaluationCWProxy {
  AnalystEvaluation since(DateTime since);

  AnalystEvaluation profiles(List<AnalystEvaluationProfilesInner> profiles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystEvaluation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystEvaluation(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystEvaluation call({
    DateTime since,
    List<AnalystEvaluationProfilesInner> profiles,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystEvaluation.copyWith(...)` or call `instanceOfAnalystEvaluation.copyWith.fieldName(value)` for a single field.
class _$AnalystEvaluationCWProxyImpl implements _$AnalystEvaluationCWProxy {
  const _$AnalystEvaluationCWProxyImpl(this._value);

  final AnalystEvaluation _value;

  @override
  AnalystEvaluation since(DateTime since) => call(since: since);

  @override
  AnalystEvaluation profiles(List<AnalystEvaluationProfilesInner> profiles) =>
      call(profiles: profiles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystEvaluation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystEvaluation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystEvaluation call({
    Object? since = const $CopyWithPlaceholder(),
    Object? profiles = const $CopyWithPlaceholder(),
  }) {
    return AnalystEvaluation(
      since: since == const $CopyWithPlaceholder() || since == null
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime,
      profiles: profiles == const $CopyWithPlaceholder() || profiles == null
          ? _value.profiles
          // ignore: cast_nullable_to_non_nullable
          : profiles as List<AnalystEvaluationProfilesInner>,
    );
  }
}

extension $AnalystEvaluationCopyWith on AnalystEvaluation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystEvaluation.copyWith(...)` or `instanceOfAnalystEvaluation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystEvaluationCWProxy get copyWith =>
      _$AnalystEvaluationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystEvaluation _$AnalystEvaluationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystEvaluation', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['since', 'profiles']);
      final val = AnalystEvaluation(
        since: $checkedConvert('since', (v) => DateTime.parse(v as String)),
        profiles: $checkedConvert(
          'profiles',
          (v) => (v as List<dynamic>)
              .map(
                (e) => AnalystEvaluationProfilesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnalystEvaluationToJson(AnalystEvaluation instance) =>
    <String, dynamic>{
      'since': instance.since.toIso8601String(),
      'profiles': instance.profiles.map((e) => e.toJson()).toList(),
    };
