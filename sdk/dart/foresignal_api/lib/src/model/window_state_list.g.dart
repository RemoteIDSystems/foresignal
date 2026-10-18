// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'window_state_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WindowStateListCWProxy {
  WindowStateList ruleId(String ruleId);

  WindowStateList type(WindowStateListTypeEnum type);

  WindowStateList groups(List<WindowState> groups);

  WindowStateList groupsTruncated(bool? groupsTruncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowStateList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowStateList(...).copyWith(id: 12, name: "My name")
  /// ```
  WindowStateList call({
    String ruleId,
    WindowStateListTypeEnum type,
    List<WindowState> groups,
    bool? groupsTruncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWindowStateList.copyWith(...)` or call `instanceOfWindowStateList.copyWith.fieldName(value)` for a single field.
class _$WindowStateListCWProxyImpl implements _$WindowStateListCWProxy {
  const _$WindowStateListCWProxyImpl(this._value);

  final WindowStateList _value;

  @override
  WindowStateList ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  WindowStateList type(WindowStateListTypeEnum type) => call(type: type);

  @override
  WindowStateList groups(List<WindowState> groups) => call(groups: groups);

  @override
  WindowStateList groupsTruncated(bool? groupsTruncated) =>
      call(groupsTruncated: groupsTruncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowStateList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowStateList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WindowStateList call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? groups = const $CopyWithPlaceholder(),
    Object? groupsTruncated = const $CopyWithPlaceholder(),
  }) {
    return WindowStateList(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as WindowStateListTypeEnum,
      groups: groups == const $CopyWithPlaceholder() || groups == null
          ? _value.groups
          // ignore: cast_nullable_to_non_nullable
          : groups as List<WindowState>,
      groupsTruncated: groupsTruncated == const $CopyWithPlaceholder()
          ? _value.groupsTruncated
          // ignore: cast_nullable_to_non_nullable
          : groupsTruncated as bool?,
    );
  }
}

extension $WindowStateListCopyWith on WindowStateList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWindowStateList.copyWith(...)` or `instanceOfWindowStateList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WindowStateListCWProxy get copyWith => _$WindowStateListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WindowStateList _$WindowStateListFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WindowStateList', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ruleId', 'type', 'groups']);
      final val = WindowStateList(
        ruleId: $checkedConvert('ruleId', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$WindowStateListTypeEnumEnumMap, v),
        ),
        groups: $checkedConvert(
          'groups',
          (v) => (v as List<dynamic>)
              .map((e) => WindowState.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        groupsTruncated: $checkedConvert('groupsTruncated', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$WindowStateListToJson(WindowStateList instance) =>
    <String, dynamic>{
      'ruleId': instance.ruleId,
      'type': _$WindowStateListTypeEnumEnumMap[instance.type]!,
      'groups': instance.groups.map((e) => e.toJson()).toList(),
      'groupsTruncated': ?instance.groupsTruncated,
    };

const _$WindowStateListTypeEnumEnumMap = {
  WindowStateListTypeEnum.count: 'count',
  WindowStateListTypeEnum.absence: 'absence',
  WindowStateListTypeEnum.anomaly: 'anomaly',
};
