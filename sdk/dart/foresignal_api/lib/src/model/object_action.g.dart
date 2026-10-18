// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_action.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectActionCWProxy {
  ObjectAction id(String id);

  ObjectAction label(String? label);

  ObjectAction method(ObjectActionMethodEnum method);

  ObjectAction href(String href);

  ObjectAction scope(ObjectActionScopeEnum scope);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAction(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectAction call({
    String id,
    String? label,
    ObjectActionMethodEnum method,
    String href,
    ObjectActionScopeEnum scope,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectAction.copyWith(...)` or call `instanceOfObjectAction.copyWith.fieldName(value)` for a single field.
class _$ObjectActionCWProxyImpl implements _$ObjectActionCWProxy {
  const _$ObjectActionCWProxyImpl(this._value);

  final ObjectAction _value;

  @override
  ObjectAction id(String id) => call(id: id);

  @override
  ObjectAction label(String? label) => call(label: label);

  @override
  ObjectAction method(ObjectActionMethodEnum method) => call(method: method);

  @override
  ObjectAction href(String href) => call(href: href);

  @override
  ObjectAction scope(ObjectActionScopeEnum scope) => call(scope: scope);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAction(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectAction call({
    Object? id = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? href = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
  }) {
    return ObjectAction(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as ObjectActionMethodEnum,
      href: href == const $CopyWithPlaceholder() || href == null
          ? _value.href
          // ignore: cast_nullable_to_non_nullable
          : href as String,
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as ObjectActionScopeEnum,
    );
  }
}

extension $ObjectActionCopyWith on ObjectAction {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectAction.copyWith(...)` or `instanceOfObjectAction.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectActionCWProxy get copyWith => _$ObjectActionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectAction _$ObjectActionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ObjectAction', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'method', 'href', 'scope']);
      final val = ObjectAction(
        id: $checkedConvert('id', (v) => v as String),
        label: $checkedConvert('label', (v) => v as String?),
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(_$ObjectActionMethodEnumEnumMap, v),
        ),
        href: $checkedConvert('href', (v) => v as String),
        scope: $checkedConvert(
          'scope',
          (v) => $enumDecode(_$ObjectActionScopeEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ObjectActionToJson(ObjectAction instance) =>
    <String, dynamic>{
      'id': instance.id,
      'label': ?instance.label,
      'method': _$ObjectActionMethodEnumEnumMap[instance.method]!,
      'href': instance.href,
      'scope': _$ObjectActionScopeEnumEnumMap[instance.scope]!,
    };

const _$ObjectActionMethodEnumEnumMap = {
  ObjectActionMethodEnum.GET: 'GET',
  ObjectActionMethodEnum.POST: 'POST',
  ObjectActionMethodEnum.PUT: 'PUT',
  ObjectActionMethodEnum.PATCH: 'PATCH',
  ObjectActionMethodEnum.DELETE: 'DELETE',
};

const _$ObjectActionScopeEnumEnumMap = {
  ObjectActionScopeEnum.read: 'read',
  ObjectActionScopeEnum.agent: 'agent',
  ObjectActionScopeEnum.admin: 'admin',
};
