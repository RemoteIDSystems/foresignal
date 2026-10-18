//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'auto_resolve.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AutoResolve {
  /// Returns a new [AutoResolve] instance.
  AutoResolve({

     this.onInverseEdge = true,

     this.onTombstone = true,

     this.onExpiry = true,

     this.staleAfter,
  });

  @JsonKey(
    defaultValue: true,
    name: r'onInverseEdge',
    required: false,
    includeIfNull: false,
  )


  final bool? onInverseEdge;



  @JsonKey(
    defaultValue: true,
    name: r'onTombstone',
    required: false,
    includeIfNull: false,
  )


  final bool? onTombstone;



  @JsonKey(
    defaultValue: true,
    name: r'onExpiry',
    required: false,
    includeIfNull: false,
  )


  final bool? onExpiry;



      /// Resolve when the entity has not been observed for this long. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'staleAfter',
    required: false,
    includeIfNull: false,
  )


  final String? staleAfter;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AutoResolve &&
      other.onInverseEdge == onInverseEdge &&
      other.onTombstone == onTombstone &&
      other.onExpiry == onExpiry &&
      other.staleAfter == staleAfter;

    @override
    int get hashCode =>
        onInverseEdge.hashCode +
        onTombstone.hashCode +
        onExpiry.hashCode +
        (staleAfter == null ? 0 : staleAfter.hashCode);

  factory AutoResolve.fromJson(Map<String, dynamic> json) => _$AutoResolveFromJson(json);

  Map<String, dynamic> toJson() => _$AutoResolveToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

