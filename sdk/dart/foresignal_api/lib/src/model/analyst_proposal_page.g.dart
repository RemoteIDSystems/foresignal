// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_proposal_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystProposalPageCWProxy {
  AnalystProposalPage items(List<AnalystProposal> items);

  AnalystProposalPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProposalPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProposalPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystProposalPage call({List<AnalystProposal> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystProposalPage.copyWith(...)` or call `instanceOfAnalystProposalPage.copyWith.fieldName(value)` for a single field.
class _$AnalystProposalPageCWProxyImpl implements _$AnalystProposalPageCWProxy {
  const _$AnalystProposalPageCWProxyImpl(this._value);

  final AnalystProposalPage _value;

  @override
  AnalystProposalPage items(List<AnalystProposal> items) => call(items: items);

  @override
  AnalystProposalPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProposalPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProposalPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystProposalPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AnalystProposalPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AnalystProposal>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AnalystProposalPageCopyWith on AnalystProposalPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystProposalPage.copyWith(...)` or `instanceOfAnalystProposalPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystProposalPageCWProxy get copyWith =>
      _$AnalystProposalPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystProposalPage _$AnalystProposalPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystProposalPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AnalystProposalPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => AnalystProposal.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AnalystProposalPageToJson(
  AnalystProposalPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
