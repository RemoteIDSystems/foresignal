// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_split_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationSplitInputCWProxy {
  SituationSplitInput memberIds(List<String> memberIds);

  SituationSplitInput title(String title);

  SituationSplitInput theme(SituationTheme? theme);

  SituationSplitInput version(int? version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSplitInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSplitInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationSplitInput call({
    List<String> memberIds,
    String title,
    SituationTheme? theme,
    int? version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationSplitInput.copyWith(...)` or call `instanceOfSituationSplitInput.copyWith.fieldName(value)` for a single field.
class _$SituationSplitInputCWProxyImpl implements _$SituationSplitInputCWProxy {
  const _$SituationSplitInputCWProxyImpl(this._value);

  final SituationSplitInput _value;

  @override
  SituationSplitInput memberIds(List<String> memberIds) =>
      call(memberIds: memberIds);

  @override
  SituationSplitInput title(String title) => call(title: title);

  @override
  SituationSplitInput theme(SituationTheme? theme) => call(theme: theme);

  @override
  SituationSplitInput version(int? version) => call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSplitInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSplitInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationSplitInput call({
    Object? memberIds = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? theme = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return SituationSplitInput(
      memberIds: memberIds == const $CopyWithPlaceholder() || memberIds == null
          ? _value.memberIds
          // ignore: cast_nullable_to_non_nullable
          : memberIds as List<String>,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      theme: theme == const $CopyWithPlaceholder()
          ? _value.theme
          // ignore: cast_nullable_to_non_nullable
          : theme as SituationTheme?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
    );
  }
}

extension $SituationSplitInputCopyWith on SituationSplitInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationSplitInput.copyWith(...)` or `instanceOfSituationSplitInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationSplitInputCWProxy get copyWith =>
      _$SituationSplitInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationSplitInput _$SituationSplitInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationSplitInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['memberIds', 'title']);
      final val = SituationSplitInput(
        memberIds: $checkedConvert(
          'memberIds',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        title: $checkedConvert('title', (v) => v as String),
        theme: $checkedConvert(
          'theme',
          (v) => $enumDecodeNullable(_$SituationThemeEnumMap, v),
        ),
        version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SituationSplitInputToJson(
  SituationSplitInput instance,
) => <String, dynamic>{
  'memberIds': instance.memberIds,
  'title': instance.title,
  'theme': ?_$SituationThemeEnumMap[instance.theme],
  'version': ?instance.version,
};

const _$SituationThemeEnumMap = {
  SituationTheme.maritime: 'maritime',
  SituationTheme.aviation: 'aviation',
  SituationTheme.conflict: 'conflict',
  SituationTheme.infrastructure: 'infrastructure',
  SituationTheme.weather: 'weather',
  SituationTheme.cyber: 'cyber',
  SituationTheme.markets: 'markets',
  SituationTheme.health: 'health',
  SituationTheme.space: 'space',
  SituationTheme.mixed: 'mixed',
};
