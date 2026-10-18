//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'propagation_edge.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PropagationEdge {
  /// Returns a new [PropagationEdge] instance.
  PropagationEdge({

    required  this.from,

    required  this.to,

    required  this.type,

    required  this.deltaSeconds,

     this.count,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final String from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final String to;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final PropagationEdgeTypeEnum type;



  @JsonKey(
    
    name: r'deltaSeconds',
    required: true,
    includeIfNull: false,
  )


  final num deltaSeconds;



      /// Edges collapsed into this one
          // minimum: 1
  @JsonKey(
    
    name: r'count',
    required: false,
    includeIfNull: false,
  )


  final int? count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PropagationEdge &&
      other.from == from &&
      other.to == to &&
      other.type == type &&
      other.deltaSeconds == deltaSeconds &&
      other.count == count;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        type.hashCode +
        deltaSeconds.hashCode +
        count.hashCode;

  factory PropagationEdge.fromJson(Map<String, dynamic> json) => _$PropagationEdgeFromJson(json);

  Map<String, dynamic> toJson() => _$PropagationEdgeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PropagationEdgeTypeEnum {
@JsonValue(r'repost')
repost(r'repost'),
@JsonValue(r'quote')
quote(r'quote'),
@JsonValue(r'reply')
reply(r'reply'),
@JsonValue(r'link')
link(r'link'),
@JsonValue(r'text-match')
textMatch(r'text-match'),
@JsonValue(r'media-match')
mediaMatch(r'media-match'),
@JsonValue(r'cross-platform')
crossPlatform(r'cross-platform');

const PropagationEdgeTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


