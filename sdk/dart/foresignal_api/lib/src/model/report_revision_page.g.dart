// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_revision_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportRevisionPageCWProxy {
  ReportRevisionPage items(List<ReportRevision> items);

  ReportRevisionPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportRevisionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportRevisionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportRevisionPage call({List<ReportRevision> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportRevisionPage.copyWith(...)` or call `instanceOfReportRevisionPage.copyWith.fieldName(value)` for a single field.
class _$ReportRevisionPageCWProxyImpl implements _$ReportRevisionPageCWProxy {
  const _$ReportRevisionPageCWProxyImpl(this._value);

  final ReportRevisionPage _value;

  @override
  ReportRevisionPage items(List<ReportRevision> items) => call(items: items);

  @override
  ReportRevisionPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportRevisionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportRevisionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportRevisionPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ReportRevisionPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ReportRevision>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ReportRevisionPageCopyWith on ReportRevisionPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportRevisionPage.copyWith(...)` or `instanceOfReportRevisionPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportRevisionPageCWProxy get copyWith =>
      _$ReportRevisionPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportRevisionPage _$ReportRevisionPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportRevisionPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = ReportRevisionPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ReportRevision.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ReportRevisionPageToJson(ReportRevisionPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
