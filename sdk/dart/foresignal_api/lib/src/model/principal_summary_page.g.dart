// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'principal_summary_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrincipalSummaryPageCWProxy {
  PrincipalSummaryPage items(List<PrincipalSummary> items);

  PrincipalSummaryPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrincipalSummaryPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrincipalSummaryPage(...).copyWith(id: 12, name: "My name")
  /// ```
  PrincipalSummaryPage call({List<PrincipalSummary> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrincipalSummaryPage.copyWith(...)` or call `instanceOfPrincipalSummaryPage.copyWith.fieldName(value)` for a single field.
class _$PrincipalSummaryPageCWProxyImpl
    implements _$PrincipalSummaryPageCWProxy {
  const _$PrincipalSummaryPageCWProxyImpl(this._value);

  final PrincipalSummaryPage _value;

  @override
  PrincipalSummaryPage items(List<PrincipalSummary> items) =>
      call(items: items);

  @override
  PrincipalSummaryPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrincipalSummaryPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrincipalSummaryPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrincipalSummaryPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PrincipalSummaryPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<PrincipalSummary>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PrincipalSummaryPageCopyWith on PrincipalSummaryPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrincipalSummaryPage.copyWith(...)` or `instanceOfPrincipalSummaryPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrincipalSummaryPageCWProxy get copyWith =>
      _$PrincipalSummaryPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrincipalSummaryPage _$PrincipalSummaryPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrincipalSummaryPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
  final val = PrincipalSummaryPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => PrincipalSummary.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PrincipalSummaryPageToJson(
  PrincipalSummaryPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
