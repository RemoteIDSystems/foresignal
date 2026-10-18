// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workspace_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WorkspacePageCWProxy {
  WorkspacePage items(List<Workspace> items);

  WorkspacePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspacePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspacePage(...).copyWith(id: 12, name: "My name")
  /// ```
  WorkspacePage call({List<Workspace> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWorkspacePage.copyWith(...)` or call `instanceOfWorkspacePage.copyWith.fieldName(value)` for a single field.
class _$WorkspacePageCWProxyImpl implements _$WorkspacePageCWProxy {
  const _$WorkspacePageCWProxyImpl(this._value);

  final WorkspacePage _value;

  @override
  WorkspacePage items(List<Workspace> items) => call(items: items);

  @override
  WorkspacePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspacePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspacePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WorkspacePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return WorkspacePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Workspace>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $WorkspacePageCopyWith on WorkspacePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWorkspacePage.copyWith(...)` or `instanceOfWorkspacePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WorkspacePageCWProxy get copyWith => _$WorkspacePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WorkspacePage _$WorkspacePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WorkspacePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = WorkspacePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Workspace.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$WorkspacePageToJson(WorkspacePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
