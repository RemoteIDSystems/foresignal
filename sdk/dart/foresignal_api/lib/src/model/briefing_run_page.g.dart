// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'briefing_run_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BriefingRunPageCWProxy {
  BriefingRunPage items(List<BriefingRun> items);

  BriefingRunPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  BriefingRunPage call({List<BriefingRun> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBriefingRunPage.copyWith(...)` or call `instanceOfBriefingRunPage.copyWith.fieldName(value)` for a single field.
class _$BriefingRunPageCWProxyImpl implements _$BriefingRunPageCWProxy {
  const _$BriefingRunPageCWProxyImpl(this._value);

  final BriefingRunPage _value;

  @override
  BriefingRunPage items(List<BriefingRun> items) => call(items: items);

  @override
  BriefingRunPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BriefingRunPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return BriefingRunPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<BriefingRun>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $BriefingRunPageCopyWith on BriefingRunPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBriefingRunPage.copyWith(...)` or `instanceOfBriefingRunPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BriefingRunPageCWProxy get copyWith => _$BriefingRunPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BriefingRunPage _$BriefingRunPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BriefingRunPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = BriefingRunPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => BriefingRun.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BriefingRunPageToJson(BriefingRunPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
