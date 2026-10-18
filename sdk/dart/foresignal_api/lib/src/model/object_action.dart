//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_action.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectAction {
  /// Returns a new [ObjectAction] instance.
  ObjectAction({

    required  this.id,

     this.label,

    required  this.method,

    required  this.href,

    required  this.scope,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final ObjectActionMethodEnum method;



  @JsonKey(
    
    name: r'href',
    required: true,
    includeIfNull: false,
  )


  final String href;



  @JsonKey(
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final ObjectActionScopeEnum scope;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectAction &&
      other.id == id &&
      other.label == label &&
      other.method == method &&
      other.href == href &&
      other.scope == scope;

    @override
    int get hashCode =>
        id.hashCode +
        label.hashCode +
        method.hashCode +
        href.hashCode +
        scope.hashCode;

  factory ObjectAction.fromJson(Map<String, dynamic> json) => _$ObjectActionFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectActionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ObjectActionMethodEnum {
@JsonValue(r'GET')
GET(r'GET'),
@JsonValue(r'POST')
POST(r'POST'),
@JsonValue(r'PUT')
PUT(r'PUT'),
@JsonValue(r'PATCH')
PATCH(r'PATCH'),
@JsonValue(r'DELETE')
DELETE(r'DELETE');

const ObjectActionMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ObjectActionScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'admin')
admin(r'admin');

const ObjectActionScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


