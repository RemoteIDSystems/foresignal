// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_series_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisSeriesPageCWProxy {
  AnalysisSeriesPage items(List<AnalysisSeries> items);

  AnalysisSeriesPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisSeriesPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisSeriesPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisSeriesPage call({List<AnalysisSeries> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisSeriesPage.copyWith(...)` or call `instanceOfAnalysisSeriesPage.copyWith.fieldName(value)` for a single field.
class _$AnalysisSeriesPageCWProxyImpl implements _$AnalysisSeriesPageCWProxy {
  const _$AnalysisSeriesPageCWProxyImpl(this._value);

  final AnalysisSeriesPage _value;

  @override
  AnalysisSeriesPage items(List<AnalysisSeries> items) => call(items: items);

  @override
  AnalysisSeriesPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisSeriesPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisSeriesPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisSeriesPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AnalysisSeriesPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AnalysisSeries>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AnalysisSeriesPageCopyWith on AnalysisSeriesPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisSeriesPage.copyWith(...)` or `instanceOfAnalysisSeriesPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisSeriesPageCWProxy get copyWith =>
      _$AnalysisSeriesPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisSeriesPage _$AnalysisSeriesPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalysisSeriesPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AnalysisSeriesPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => AnalysisSeries.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AnalysisSeriesPageToJson(AnalysisSeriesPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
