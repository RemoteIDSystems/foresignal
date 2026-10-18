// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'finding_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FindingPageCWProxy {
  FindingPage items(List<AnalyticFinding> items);

  FindingPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingPage(...).copyWith(id: 12, name: "My name")
  /// ```
  FindingPage call({List<AnalyticFinding> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFindingPage.copyWith(...)` or call `instanceOfFindingPage.copyWith.fieldName(value)` for a single field.
class _$FindingPageCWProxyImpl implements _$FindingPageCWProxy {
  const _$FindingPageCWProxyImpl(this._value);

  final FindingPage _value;

  @override
  FindingPage items(List<AnalyticFinding> items) => call(items: items);

  @override
  FindingPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FindingPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return FindingPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AnalyticFinding>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $FindingPageCopyWith on FindingPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFindingPage.copyWith(...)` or `instanceOfFindingPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FindingPageCWProxy get copyWith => _$FindingPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FindingPage _$FindingPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FindingPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = FindingPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => AnalyticFinding.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$FindingPageToJson(FindingPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
