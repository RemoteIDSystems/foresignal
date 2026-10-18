// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tuning_suggestion_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TuningSuggestionListCWProxy {
  TuningSuggestionList ruleId(String ruleId);

  TuningSuggestionList since(DateTime since);

  TuningSuggestionList suggesterVersion(String suggesterVersion);

  TuningSuggestionList suggestions(List<TuningSuggestionRecord> suggestions);

  TuningSuggestionList simulationJobId(String? simulationJobId);

  TuningSuggestionList jobId(String? jobId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionList(...).copyWith(id: 12, name: "My name")
  /// ```
  TuningSuggestionList call({
    String ruleId,
    DateTime since,
    String suggesterVersion,
    List<TuningSuggestionRecord> suggestions,
    String? simulationJobId,
    String? jobId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTuningSuggestionList.copyWith(...)` or call `instanceOfTuningSuggestionList.copyWith.fieldName(value)` for a single field.
class _$TuningSuggestionListCWProxyImpl
    implements _$TuningSuggestionListCWProxy {
  const _$TuningSuggestionListCWProxyImpl(this._value);

  final TuningSuggestionList _value;

  @override
  TuningSuggestionList ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  TuningSuggestionList since(DateTime since) => call(since: since);

  @override
  TuningSuggestionList suggesterVersion(String suggesterVersion) =>
      call(suggesterVersion: suggesterVersion);

  @override
  TuningSuggestionList suggestions(List<TuningSuggestionRecord> suggestions) =>
      call(suggestions: suggestions);

  @override
  TuningSuggestionList simulationJobId(String? simulationJobId) =>
      call(simulationJobId: simulationJobId);

  @override
  TuningSuggestionList jobId(String? jobId) => call(jobId: jobId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TuningSuggestionList call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
    Object? suggesterVersion = const $CopyWithPlaceholder(),
    Object? suggestions = const $CopyWithPlaceholder(),
    Object? simulationJobId = const $CopyWithPlaceholder(),
    Object? jobId = const $CopyWithPlaceholder(),
  }) {
    return TuningSuggestionList(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      since: since == const $CopyWithPlaceholder() || since == null
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime,
      suggesterVersion:
          suggesterVersion == const $CopyWithPlaceholder() ||
              suggesterVersion == null
          ? _value.suggesterVersion
          // ignore: cast_nullable_to_non_nullable
          : suggesterVersion as String,
      suggestions:
          suggestions == const $CopyWithPlaceholder() || suggestions == null
          ? _value.suggestions
          // ignore: cast_nullable_to_non_nullable
          : suggestions as List<TuningSuggestionRecord>,
      simulationJobId: simulationJobId == const $CopyWithPlaceholder()
          ? _value.simulationJobId
          // ignore: cast_nullable_to_non_nullable
          : simulationJobId as String?,
      jobId: jobId == const $CopyWithPlaceholder()
          ? _value.jobId
          // ignore: cast_nullable_to_non_nullable
          : jobId as String?,
    );
  }
}

extension $TuningSuggestionListCopyWith on TuningSuggestionList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTuningSuggestionList.copyWith(...)` or `instanceOfTuningSuggestionList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TuningSuggestionListCWProxy get copyWith =>
      _$TuningSuggestionListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TuningSuggestionList _$TuningSuggestionListFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TuningSuggestionList', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['ruleId', 'since', 'suggesterVersion', 'suggestions'],
  );
  final val = TuningSuggestionList(
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    since: $checkedConvert('since', (v) => DateTime.parse(v as String)),
    suggesterVersion: $checkedConvert('suggesterVersion', (v) => v as String),
    suggestions: $checkedConvert(
      'suggestions',
      (v) => (v as List<dynamic>)
          .map(
            (e) => TuningSuggestionRecord.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    simulationJobId: $checkedConvert('simulationJobId', (v) => v as String?),
    jobId: $checkedConvert('jobId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$TuningSuggestionListToJson(
  TuningSuggestionList instance,
) => <String, dynamic>{
  'ruleId': instance.ruleId,
  'since': instance.since.toIso8601String(),
  'suggesterVersion': instance.suggesterVersion,
  'suggestions': instance.suggestions.map((e) => e.toJson()).toList(),
  'simulationJobId': ?instance.simulationJobId,
  'jobId': ?instance.jobId,
};
