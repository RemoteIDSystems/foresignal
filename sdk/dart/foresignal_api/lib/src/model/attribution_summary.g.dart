// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attribution_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttributionSummaryCWProxy {
  AttributionSummary attributions(List<Attribution> attributions);

  AttributionSummary text(String text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttributionSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttributionSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  AttributionSummary call({List<Attribution> attributions, String text});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttributionSummary.copyWith(...)` or call `instanceOfAttributionSummary.copyWith.fieldName(value)` for a single field.
class _$AttributionSummaryCWProxyImpl implements _$AttributionSummaryCWProxy {
  const _$AttributionSummaryCWProxyImpl(this._value);

  final AttributionSummary _value;

  @override
  AttributionSummary attributions(List<Attribution> attributions) =>
      call(attributions: attributions);

  @override
  AttributionSummary text(String text) => call(text: text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttributionSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttributionSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AttributionSummary call({
    Object? attributions = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
  }) {
    return AttributionSummary(
      attributions:
          attributions == const $CopyWithPlaceholder() || attributions == null
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
    );
  }
}

extension $AttributionSummaryCopyWith on AttributionSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttributionSummary.copyWith(...)` or `instanceOfAttributionSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttributionSummaryCWProxy get copyWith =>
      _$AttributionSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AttributionSummary _$AttributionSummaryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AttributionSummary', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['attributions', 'text']);
      final val = AttributionSummary(
        attributions: $checkedConvert(
          'attributions',
          (v) => (v as List<dynamic>)
              .map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        text: $checkedConvert('text', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$AttributionSummaryToJson(AttributionSummary instance) =>
    <String, dynamic>{
      'attributions': instance.attributions.map((e) => e.toJson()).toList(),
      'text': instance.text,
    };
