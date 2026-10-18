// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'investigation_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InvestigationPageCWProxy {
  InvestigationPage items(List<Investigation> items);

  InvestigationPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  InvestigationPage call({List<Investigation> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInvestigationPage.copyWith(...)` or call `instanceOfInvestigationPage.copyWith.fieldName(value)` for a single field.
class _$InvestigationPageCWProxyImpl implements _$InvestigationPageCWProxy {
  const _$InvestigationPageCWProxyImpl(this._value);

  final InvestigationPage _value;

  @override
  InvestigationPage items(List<Investigation> items) => call(items: items);

  @override
  InvestigationPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InvestigationPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return InvestigationPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Investigation>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $InvestigationPageCopyWith on InvestigationPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInvestigationPage.copyWith(...)` or `instanceOfInvestigationPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InvestigationPageCWProxy get copyWith =>
      _$InvestigationPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InvestigationPage _$InvestigationPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InvestigationPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = InvestigationPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Investigation.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$InvestigationPageToJson(InvestigationPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
