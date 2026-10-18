// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_run_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystRunPageCWProxy {
  AnalystRunPage items(List<AnalystRun> items);

  AnalystRunPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystRunPage call({List<AnalystRun> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystRunPage.copyWith(...)` or call `instanceOfAnalystRunPage.copyWith.fieldName(value)` for a single field.
class _$AnalystRunPageCWProxyImpl implements _$AnalystRunPageCWProxy {
  const _$AnalystRunPageCWProxyImpl(this._value);

  final AnalystRunPage _value;

  @override
  AnalystRunPage items(List<AnalystRun> items) => call(items: items);

  @override
  AnalystRunPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystRunPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AnalystRunPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AnalystRun>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AnalystRunPageCopyWith on AnalystRunPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystRunPage.copyWith(...)` or `instanceOfAnalystRunPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystRunPageCWProxy get copyWith => _$AnalystRunPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystRunPage _$AnalystRunPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystRunPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AnalystRunPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => AnalystRun.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AnalystRunPageToJson(AnalystRunPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
