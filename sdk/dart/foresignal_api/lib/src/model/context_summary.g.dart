// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'context_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContextSummaryCWProxy {
  ContextSummary context(ContextSummaryRequest? context);

  ContextSummary generatedAt(DateTime generatedAt);

  ContextSummary cacheAgeSeconds(int? cacheAgeSeconds);

  ContextSummary layerCounts(List<ContextSummaryLayerCountsInner> layerCounts);

  ContextSummary activity(List<ContextSummaryActivityInner> activity);

  ContextSummary insights(List<ContextSummaryInsightsInner> insights);

  ContextSummary related(List<ObjectEnvelope> related);

  ContextSummary narrative(ContextSummaryNarrative? narrative);

  ContextSummary truncated(bool? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  ContextSummary call({
    ContextSummaryRequest? context,
    DateTime generatedAt,
    int? cacheAgeSeconds,
    List<ContextSummaryLayerCountsInner> layerCounts,
    List<ContextSummaryActivityInner> activity,
    List<ContextSummaryInsightsInner> insights,
    List<ObjectEnvelope> related,
    ContextSummaryNarrative? narrative,
    bool? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContextSummary.copyWith(...)` or call `instanceOfContextSummary.copyWith.fieldName(value)` for a single field.
class _$ContextSummaryCWProxyImpl implements _$ContextSummaryCWProxy {
  const _$ContextSummaryCWProxyImpl(this._value);

  final ContextSummary _value;

  @override
  ContextSummary context(ContextSummaryRequest? context) =>
      call(context: context);

  @override
  ContextSummary generatedAt(DateTime generatedAt) =>
      call(generatedAt: generatedAt);

  @override
  ContextSummary cacheAgeSeconds(int? cacheAgeSeconds) =>
      call(cacheAgeSeconds: cacheAgeSeconds);

  @override
  ContextSummary layerCounts(
    List<ContextSummaryLayerCountsInner> layerCounts,
  ) => call(layerCounts: layerCounts);

  @override
  ContextSummary activity(List<ContextSummaryActivityInner> activity) =>
      call(activity: activity);

  @override
  ContextSummary insights(List<ContextSummaryInsightsInner> insights) =>
      call(insights: insights);

  @override
  ContextSummary related(List<ObjectEnvelope> related) =>
      call(related: related);

  @override
  ContextSummary narrative(ContextSummaryNarrative? narrative) =>
      call(narrative: narrative);

  @override
  ContextSummary truncated(bool? truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ContextSummary call({
    Object? context = const $CopyWithPlaceholder(),
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? cacheAgeSeconds = const $CopyWithPlaceholder(),
    Object? layerCounts = const $CopyWithPlaceholder(),
    Object? activity = const $CopyWithPlaceholder(),
    Object? insights = const $CopyWithPlaceholder(),
    Object? related = const $CopyWithPlaceholder(),
    Object? narrative = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return ContextSummary(
      context: context == const $CopyWithPlaceholder()
          ? _value.context
          // ignore: cast_nullable_to_non_nullable
          : context as ContextSummaryRequest?,
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
      cacheAgeSeconds: cacheAgeSeconds == const $CopyWithPlaceholder()
          ? _value.cacheAgeSeconds
          // ignore: cast_nullable_to_non_nullable
          : cacheAgeSeconds as int?,
      layerCounts:
          layerCounts == const $CopyWithPlaceholder() || layerCounts == null
          ? _value.layerCounts
          // ignore: cast_nullable_to_non_nullable
          : layerCounts as List<ContextSummaryLayerCountsInner>,
      activity: activity == const $CopyWithPlaceholder() || activity == null
          ? _value.activity
          // ignore: cast_nullable_to_non_nullable
          : activity as List<ContextSummaryActivityInner>,
      insights: insights == const $CopyWithPlaceholder() || insights == null
          ? _value.insights
          // ignore: cast_nullable_to_non_nullable
          : insights as List<ContextSummaryInsightsInner>,
      related: related == const $CopyWithPlaceholder() || related == null
          ? _value.related
          // ignore: cast_nullable_to_non_nullable
          : related as List<ObjectEnvelope>,
      narrative: narrative == const $CopyWithPlaceholder()
          ? _value.narrative
          // ignore: cast_nullable_to_non_nullable
          : narrative as ContextSummaryNarrative?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
    );
  }
}

extension $ContextSummaryCopyWith on ContextSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContextSummary.copyWith(...)` or `instanceOfContextSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContextSummaryCWProxy get copyWith => _$ContextSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ContextSummary _$ContextSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ContextSummary', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'generatedAt',
      'layerCounts',
      'activity',
      'insights',
      'related',
    ],
  );
  final val = ContextSummary(
    context: $checkedConvert(
      'context',
      (v) => v == null
          ? null
          : ContextSummaryRequest.fromJson(v as Map<String, dynamic>),
    ),
    generatedAt: $checkedConvert(
      'generatedAt',
      (v) => DateTime.parse(v as String),
    ),
    cacheAgeSeconds: $checkedConvert(
      'cacheAgeSeconds',
      (v) => (v as num?)?.toInt(),
    ),
    layerCounts: $checkedConvert(
      'layerCounts',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ContextSummaryLayerCountsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    activity: $checkedConvert(
      'activity',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                ContextSummaryActivityInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    insights: $checkedConvert(
      'insights',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                ContextSummaryInsightsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    related: $checkedConvert(
      'related',
      (v) => (v as List<dynamic>)
          .map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    narrative: $checkedConvert(
      'narrative',
      (v) => v == null
          ? null
          : ContextSummaryNarrative.fromJson(v as Map<String, dynamic>),
    ),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$ContextSummaryToJson(ContextSummary instance) =>
    <String, dynamic>{
      'context': ?instance.context?.toJson(),
      'generatedAt': instance.generatedAt.toIso8601String(),
      'cacheAgeSeconds': ?instance.cacheAgeSeconds,
      'layerCounts': instance.layerCounts.map((e) => e.toJson()).toList(),
      'activity': instance.activity.map((e) => e.toJson()).toList(),
      'insights': instance.insights.map((e) => e.toJson()).toList(),
      'related': instance.related.map((e) => e.toJson()).toList(),
      'narrative': ?instance.narrative?.toJson(),
      'truncated': ?instance.truncated,
    };
